package com.example.minirpc.core.server;

/**
 * RPC服务器接口
 */
public interface RpcServer {
    
    /**
     * 启动RPC服务器
     */
    void start();
    
    /**
     * 关闭RPC服务器
     */
    void stop();
    
    /**
     * 注册服务
     * 
     * @param serviceName 服务名称
     * @param serviceBean 服务实例
     */
    void registerService(String serviceName, Object serviceBean);
}
