package com.example.minirpc.core.registry;

/**
 * 服务注册接口
 */
public interface ServiceRegistry {

    /**
     * 注册服务
     * 
     * @param serviceName 服务名称
     * @param serviceAddress 服务地址（格式：host:port）
     */
    void register(String serviceName, String serviceAddress);

    /**
     * 获取服务地址
     * 
     * @param serviceName 服务名称
     * @return 服务地址（格式：host:port）
     */
    String discover(String serviceName);
}
