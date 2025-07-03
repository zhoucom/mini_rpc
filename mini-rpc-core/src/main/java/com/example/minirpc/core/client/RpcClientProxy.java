package com.example.minirpc.core.client;

import com.example.minirpc.core.protocol.RpcRequest;
import com.example.minirpc.core.protocol.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RPC客户端代理工厂，用于创建RPC服务接口的代理实现
 */
public class RpcClientProxy {
    
    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);
    
    private final RpcClient client;
    private final String version;
    
    public RpcClientProxy(RpcClient client, String version) {
        this.client = client;
        this.version = version;
    }
    
    /**
     * 创建指定接口的代理实现
     * 
     * @param interfaceClass 接口类
     * @param <T> 接口类型
     * @return 接口代理实现
     */
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[] { interfaceClass },
                new RpcInvocationHandler(interfaceClass)
        );
    }
    
    /**
     * RPC方法调用处理器
     */
    private class RpcInvocationHandler implements InvocationHandler {
        private final Class<?> interfaceClass;
        
        public RpcInvocationHandler(Class<?> interfaceClass) {
            this.interfaceClass = interfaceClass;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 对Object类的方法直接调用，不走RPC
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            
            // 构建RPC请求
            RpcRequest request = new RpcRequest();
            request.setRequestId(UUID.randomUUID().toString());
            request.setInterfaceName(interfaceClass.getName());
            request.setMethodName(method.getName());
            request.setParameterTypes(method.getParameterTypes());
            request.setParameters(args);
            
            logger.debug("发起RPC调用: {}.{}()", interfaceClass.getSimpleName(), method.getName());
            
            // 发送RPC请求
            RpcResponse<?> response = client.send(request);
            
            // 处理响应
            if (response.getCode() != 0) {
                logger.error("RPC调用失败: {} - {}", response.getCode(), response.getMessage());
                throw new RuntimeException("RPC调用失败: " + response.getMessage());
            }
            
            return response.getData();
        }
    }
}
