package com.example.minirpc.sample.provider;

import com.example.minirpc.core.annotation.RpcService;
import com.example.minirpc.sample.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * HelloService接口实现
 */
@Component  // 添加Spring @Component注解，确保Spring能识别这个类
@RpcService(serviceInterface = HelloService.class)
public class HelloServiceImpl implements HelloService {
    
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);
    
    @Override
    public String hello(String name) {
        logger.info("收到来自客户端的请求: {}", name);
        return "你好, " + name + "! 欢迎使用Mini RPC框架!";
    }
    
    @Override
    public String getServerTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "服务器当前时间: " + formatter.format(new Date());
    }
}
