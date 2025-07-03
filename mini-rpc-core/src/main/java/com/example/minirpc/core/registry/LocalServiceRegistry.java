package com.example.minirpc.core.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的本地内存服务注册实现
 */
public class LocalServiceRegistry implements ServiceRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalServiceRegistry.class);
    
    private final Map<String, String> serviceAddressMap = new ConcurrentHashMap<>();

    @Override
    public void register(String serviceName, String serviceAddress) {
        logger.info("注册服务: {} -> {}", serviceName, serviceAddress);
        serviceAddressMap.put(serviceName, serviceAddress);
    }

    @Override
    public String discover(String serviceName) {
        logger.info("发现服务: {}", serviceName);
        return serviceAddressMap.get(serviceName);
    }
}
