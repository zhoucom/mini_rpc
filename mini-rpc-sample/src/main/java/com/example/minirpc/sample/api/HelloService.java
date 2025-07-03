package com.example.minirpc.sample.api;

/**
 * 示例服务接口
 */
public interface HelloService {
    
    /**
     * 简单的问候方法
     *
     * @param name 名称
     * @return 问候语
     */
    String hello(String name);
    
    /**
     * 获取当前服务器时间
     *
     * @return 服务器时间的字符串表示
     */
    String getServerTime();
}
