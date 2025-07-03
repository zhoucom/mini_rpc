package com.example.minirpc.starter.autoconfigure;

import com.example.minirpc.core.annotation.RpcReference;
import com.example.minirpc.core.annotation.RpcService;
import com.example.minirpc.core.client.NettyRpcClient;
import com.example.minirpc.core.client.RpcClient;
import com.example.minirpc.core.client.RpcClientProxy;
import com.example.minirpc.core.registry.DirectServiceRegistry;
import com.example.minirpc.core.registry.LocalServiceRegistry;
import com.example.minirpc.core.registry.ServiceRegistry;
import com.example.minirpc.core.serialize.JsonSerializer;
import com.example.minirpc.core.serialize.Serializer;
import com.example.minirpc.core.server.NettyRpcServer;
import com.example.minirpc.core.server.RpcServer;
import com.example.minirpc.starter.properties.MiniRpcProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Mini RPC框架自动配置类
 */
@Configuration
@EnableConfigurationProperties(MiniRpcProperties.class)
public class MiniRpcAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(MiniRpcAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public Serializer serializer() {
        return new JsonSerializer();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceRegistry serviceRegistry(MiniRpcProperties properties) {
        // 如果配置了直连地址，则使用DirectServiceRegistry
        if (properties.getDirectAddress() != null && !properties.getDirectAddress().isEmpty()) {
            logger.info("使用直连服务注册表, 地址: {}", properties.getDirectAddress());
            return new DirectServiceRegistry(properties.getDirectAddress());
        }
        // 否则使用本地内存注册表
        logger.info("使用本地内存服务注册表");
        return new LocalServiceRegistry();
    }

    /**
     * RPC服务器配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "mini.rpc", name = "server-enable", havingValue = "true")
    public static class ServerConfig {
        
        private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);

        @Autowired
        private MiniRpcProperties properties;

        @Bean
        @ConditionalOnMissingBean
        public RpcServer rpcServer(ServiceRegistry serviceRegistry, Serializer serializer) {
            String host = properties.getServer().getHost();
            int port = properties.getServer().getPort();
            return new NettyRpcServer(host, port, serviceRegistry, serializer);
        }

        /**
         * RPC服务注册处理器
         */
        @Bean
        public RpcServiceBeanPostProcessor rpcServiceBeanPostProcessor(RpcServer rpcServer) {
            return new RpcServiceBeanPostProcessor(rpcServer);
        }

        /**
         * 处理带有@RpcService注解的Bean，将其注册为RPC服务
         */
        public static class RpcServiceBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {
            private static final Logger logger = LoggerFactory.getLogger(RpcServiceBeanPostProcessor.class);
            private ApplicationContext context;
            private final RpcServer rpcServer;

            public RpcServiceBeanPostProcessor(RpcServer rpcServer) {
                this.rpcServer = rpcServer;
            }

            @Override
            public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
                this.context = applicationContext;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                // 获取真实的Bean类型
                Class<?> clazz = AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass();
                
                RpcService annotation = clazz.getAnnotation(RpcService.class);
                if (annotation != null) {
                    Class<?> serviceInterface;
                    if (annotation.serviceInterface() != void.class) {
                        // 如果指定了接口，则使用指定的接口
                        serviceInterface = annotation.serviceInterface();
                    } else {
                        // 否则使用第一个接口
                        Class<?>[] interfaces = clazz.getInterfaces();
                        if (interfaces.length == 0) {
                            throw new IllegalStateException("服务 " + clazz.getName() + " 未实现任何接口");
                        }
                        serviceInterface = interfaces[0];
                    }
                    
                    String version = annotation.version();
                    String serviceName = serviceInterface.getName();
                    if (version != null && !version.isEmpty()) {
                        serviceName += "-" + version;
                    }
                    
                    // 注册RPC服务
                    logger.info("注册RPC服务: {}", serviceName);
                    rpcServer.registerService(serviceName, bean);
                }
                return bean;
            }
        }
        
        /**
         * 启动RPC服务器
         */
        @Bean(initMethod = "start", destroyMethod = "stop")
        public RpcServerRunner rpcServerRunner(RpcServer rpcServer) {
            return new RpcServerRunner(rpcServer);
        }
        
        /**
         * RPC服务器启动器
         */
        public static class RpcServerRunner {
            private static final Logger logger = LoggerFactory.getLogger(RpcServerRunner.class);
            private final RpcServer rpcServer;

            public RpcServerRunner(RpcServer rpcServer) {
                this.rpcServer = rpcServer;
            }

            public void start() {
                // 异步启动RPC服务器
                new Thread(() -> {
                    logger.info("开始启动RPC服务器");
                    rpcServer.start();
                }, "RPC-Server").start();
            }

            public void stop() {
                rpcServer.stop();
            }
        }
    }

    /**
     * RPC客户端配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "mini.rpc", name = "client-enable", havingValue = "true")
    public static class ClientConfig {
        
        private static final Logger logger = LoggerFactory.getLogger(ClientConfig.class);
        
        @Bean
        @ConditionalOnMissingBean
        public RpcClient rpcClient(ServiceRegistry serviceRegistry, Serializer serializer) {
            return new NettyRpcClient(serviceRegistry, serializer);
        }

        /**
         * RPC引用处理器
         */
        @Bean
        public RpcReferenceBeanPostProcessor rpcReferenceBeanPostProcessor(RpcClient rpcClient) {
            return new RpcReferenceBeanPostProcessor(rpcClient);
        }

        /**
         * 处理带有@RpcReference注解的字段，注入RPC代理对象
         */
        public static class RpcReferenceBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {
            private static final Logger logger = LoggerFactory.getLogger(RpcReferenceBeanPostProcessor.class);
            private BeanFactory beanFactory;
            private final RpcClient rpcClient;

            public RpcReferenceBeanPostProcessor(RpcClient rpcClient) {
                this.rpcClient = rpcClient;
            }

            @Override
            public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
                this.beanFactory = beanFactory;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                Class<?> clazz = AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass();
                
                ReflectionUtils.doWithFields(clazz, field -> {
                    RpcReference annotation = field.getAnnotation(RpcReference.class);
                    if (annotation != null) {
                        field.setAccessible(true);
                        
                        // 获取字段类型
                        Class<?> fieldType = field.getType();
                        // 获取服务版本
                        String version = annotation.version();
                        
                        // 创建代理对象
                        RpcClientProxy proxy = new RpcClientProxy(rpcClient, version);
                        Object proxyInstance = proxy.create(fieldType);
                        
                        // 注入代理对象
                        field.set(bean, proxyInstance);
                        logger.info("注入RPC代理: {} -> {}", fieldType.getName(), bean.getClass().getName());
                    }
                });
                
                return bean;
            }
        }
    }
}
