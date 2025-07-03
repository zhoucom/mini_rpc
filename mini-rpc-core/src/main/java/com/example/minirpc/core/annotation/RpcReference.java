package com.example.minirpc.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个字段为RPC引用，将生成代理对象注入
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {

    /**
     * 服务版本号
     */
    String version() default "";

    /**
     * 超时时间，单位毫秒
     */
    long timeout() default 5000;
}
