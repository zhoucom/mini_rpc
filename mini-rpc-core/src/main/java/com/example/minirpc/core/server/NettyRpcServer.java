package com.example.minirpc.core.server;

import com.example.minirpc.core.protocol.RpcRequest;
import com.example.minirpc.core.protocol.RpcResponse;
import com.example.minirpc.core.registry.ServiceRegistry;
import com.example.minirpc.core.serialize.Serializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于Netty的RPC服务器实现
 */
public class NettyRpcServer implements RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);

    private final String host;
    private final int port;
    private final ServiceRegistry serviceRegistry;
    private final Serializer serializer;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;
    
    // 存储服务名称与服务对象的映射
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    public NettyRpcServer(String host, int port, ServiceRegistry serviceRegistry, Serializer serializer) {
        this.host = host;
        this.port = port;
        this.serviceRegistry = serviceRegistry;
        this.serializer = serializer;
    }

    @Override
    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new RpcDecoder())
                                    .addLast(new RpcEncoder())
                                    .addLast(new RpcServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口并启动服务器
            ChannelFuture future = bootstrap.bind(host, port).sync();
            logger.info("RPC服务器启动成功，监听地址：{}:{}", host, port);
            
            // 注册服务地址到注册中心
            String serverAddress = host + ":" + port;
            serviceMap.keySet().forEach(serviceName -> 
                    serviceRegistry.register(serviceName, serverAddress));
                    
            channel = future.channel();
            
            // 等待服务器关闭
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("RPC服务器启动异常", e);
        }
    }

    @Override
    public void stop() {
        if (channel != null) {
            channel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        logger.info("RPC服务器已关闭");
    }

    @Override
    public void registerService(String serviceName, Object serviceBean) {
        logger.info("注册服务: {}", serviceName);
        serviceMap.put(serviceName, serviceBean);
    }

    /**
     * RPC请求处理器
     */
    private class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) {
            // 创建并提交任务，异步处理RPC请求
            ctx.executor().execute(() -> {
                logger.debug("接收到RPC请求: {}", request);
                RpcResponse<?> response = handleRequest(request);
                ctx.writeAndFlush(response);
            });
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("RPC服务器异常", cause);
            ctx.close();
        }
    }

    /**
     * 处理RPC请求并返回结果
     */
    private RpcResponse<?> handleRequest(RpcRequest request) {
        String serviceName = request.getInterfaceName();
        Object serviceBean = serviceMap.get(serviceName);
        
        if (serviceBean == null) {
            logger.error("找不到服务: {}", serviceName);
            return RpcResponse.fail(request.getRequestId(), 404, "服务不存在: " + serviceName);
        }
        
        try {
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = request.getMethodName();
            Class<?>[] parameterTypes = request.getParameterTypes();
            Object[] parameters = request.getParameters();
            
            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            Object result = method.invoke(serviceBean, parameters);
            
            return RpcResponse.success(request.getRequestId(), result);
        } catch (Exception e) {
            logger.error("处理RPC请求时发生异常", e);
            return RpcResponse.fail(request.getRequestId(), 500, "服务调用异常: " + e.getMessage());
        }
    }

    /**
     * RPC解码器，将字节转换为RPC请求对象
     */
    private class RpcDecoder extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            // 至少需要4个字节来表示内容长度
            if (in.readableBytes() < 4) {
                return;
            }
            
            // 标记当前读取位置
            in.markReaderIndex();
            
            // 读取内容长度
            int length = in.readInt();
            
            // 检查是否有足够的数据
            if (in.readableBytes() < length) {
                in.resetReaderIndex();
                return;
            }
            
            // 读取消息体
            byte[] data = new byte[length];
            in.readBytes(data);
            
            // 反序列化为RPC请求对象
            RpcRequest request = serializer.deserialize(data, RpcRequest.class);
            out.add(request);
        }
    }

    /**
     * RPC编码器，将RPC响应对象转换为字节
     */
    private class RpcEncoder extends MessageToByteEncoder<RpcResponse> {
        @Override
        protected void encode(ChannelHandlerContext ctx, RpcResponse response, ByteBuf out) {
            // 将响应对象序列化为字节数组
            byte[] data = serializer.serialize(response);
            
            // 写入长度和数据
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
