package com.example.minirpc.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个服务实现类为RPC服务，将被暴露为远程服务
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {

    /**
     * 服务接口类，默认使用类实现的第一个接口
     */
    Class<?> serviceInterface() default void.class;

    /**
     * 服务版本号
     */
    String version() default "";
}
