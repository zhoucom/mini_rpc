package com.example.minirpc.core.client;

import com.example.minirpc.core.protocol.RpcRequest;
import com.example.minirpc.core.protocol.RpcResponse;
import com.example.minirpc.core.registry.ServiceRegistry;
import com.example.minirpc.core.serialize.Serializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 基于Netty的RPC客户端实现
 */
public class NettyRpcClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcClient.class);
    private final ServiceRegistry serviceRegistry;
    private final Serializer serializer;
    private final EventLoopGroup eventLoopGroup;
    private final Bootstrap bootstrap;
    private final Map<String, Channel> channelMap = new ConcurrentHashMap<>();
    
    // 存储未完成的请求，requestId -> 响应future
    private final Map<String, CompletableFuture<RpcResponse<?>>> pendingRequests = new ConcurrentHashMap<>();

    public NettyRpcClient(ServiceRegistry serviceRegistry, Serializer serializer) {
        this.serviceRegistry = serviceRegistry;
        this.serializer = serializer;
        this.eventLoopGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new RpcEncoder())
                                .addLast(new RpcDecoder())
                                .addLast(new RpcClientHandler());
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
    }

    @Override
    public RpcResponse<?> send(RpcRequest request) {
        String serviceName = request.getInterfaceName();
        String serviceAddress = serviceRegistry.discover(serviceName);
        
        if (serviceAddress == null) {
            logger.error("找不到服务地址: {}", serviceName);
            return RpcResponse.fail(request.getRequestId(), 404, "找不到服务地址: " + serviceName);
        }
        
        try {
            String[] parts = serviceAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            
            // 获取或创建Channel
            Channel channel = getChannel(host, port);
            if (!channel.isActive()) {
                logger.error("无法连接到服务器: {}:{}", host, port);
                return RpcResponse.fail(request.getRequestId(), 503, "无法连接到服务器");
            }
            
            // 创建响应Future
            CompletableFuture<RpcResponse<?>> responseFuture = new CompletableFuture<>();
            pendingRequests.put(request.getRequestId(), responseFuture);
            
            // 发送请求
            channel.writeAndFlush(request).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.debug("发送RPC请求成功: {}", request.getRequestId());
                } else {
                    logger.error("发送RPC请求失败", future.cause());
                    pendingRequests.remove(request.getRequestId());
                    responseFuture.completeExceptionally(future.cause());
                }
            });
            
            // 等待响应，默认5秒超时
            return responseFuture.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("发送RPC请求时发生异常", e);
            return RpcResponse.fail(request.getRequestId(), 500, "客户端异常: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        channelMap.values().forEach(Channel::close);
        eventLoopGroup.shutdownGracefully();
        logger.info("RPC客户端已关闭");
    }

    /**
     * 获取到指定服务器的Channel
     */
    private Channel getChannel(String host, int port) throws InterruptedException {
        String key = host + ":" + port;
        
        // 已有可用的Channel则直接返回
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            }
            channelMap.remove(key);
        }
        
        // 创建新的Channel
        ChannelFuture future = bootstrap.connect(host, port).sync();
        if (future.isSuccess()) {
            Channel channel = future.channel();
            channelMap.put(key, channel);
            return channel;
        }
        
        throw new RuntimeException("连接服务器失败: " + host + ":" + port);
    }

    /**
     * RPC客户端处理器
     */
    private class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse<?>> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcResponse<?> response) {
            String requestId = response.getRequestId();
            CompletableFuture<RpcResponse<?>> future = pendingRequests.remove(requestId);
            
            if (future != null) {
                future.complete(response);
            } else {
                logger.warn("收到了无效的RPC响应: {}", requestId);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("RPC客户端异常", cause);
            ctx.close();
        }
    }

    /**
     * RPC编码器，将RPC请求对象转换为字节
     */
    private class RpcEncoder extends MessageToByteEncoder<RpcRequest> {
        @Override
        protected void encode(ChannelHandlerContext ctx, RpcRequest request, ByteBuf out) {
            // 将请求对象序列化为字节数组
            byte[] data = serializer.serialize(request);
            
            // 写入长度和数据
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }

    /**
     * RPC解码器，将字节转换为RPC响应对象
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
            
            // 反序列化为RPC响应对象
            RpcResponse<?> response = serializer.deserialize(data, RpcResponse.class);
            out.add(response);
        }
    }
}
