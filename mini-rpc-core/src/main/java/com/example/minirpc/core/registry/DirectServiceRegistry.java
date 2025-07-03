package com.example.minirpc.core.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 直连服务注册表实现，使用固定的服务地址
 */
public class DirectServiceRegistry implements ServiceRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(DirectServiceRegistry.class);
    
    private final Map<String, String> serviceAddressMap = new ConcurrentHashMap<>();
    private final String defaultAddress;
    
    /**
     * 构造函数
     * 
     * @param defaultAddress 默认服务地址，格式为 host:port
     */
    public DirectServiceRegistry(String defaultAddress) {
        this.defaultAddress = defaultAddress;
        logger.info("创建直连服务注册表，默认地址: {}", defaultAddress);
    }

    @Override
    public void register(String serviceName, String serviceAddress) {
        logger.info("注册服务: {} -> {}", serviceName, serviceAddress);
        serviceAddressMap.put(serviceName, serviceAddress);
    }

    @Override
    public String discover(String serviceName) {
        String address = serviceAddressMap.getOrDefault(serviceName, defaultAddress);
        logger.info("发现服务: {} -> {}", serviceName, address);
        return address;
    }
}
