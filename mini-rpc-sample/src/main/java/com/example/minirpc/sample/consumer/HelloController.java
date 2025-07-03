package com.example.minirpc.sample.consumer;

import com.example.minirpc.core.annotation.RpcReference;
import com.example.minirpc.sample.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RPC服务消费者，通过HTTP接口调用RPC服务
 */
@RestController
@RequestMapping("/hello")
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @RpcReference
    private HelloService helloService;

    @GetMapping("/say/{name}")
    public String sayHello(@PathVariable("name") String name) {
        logger.info("调用RPC服务: hello({})", name);
        return helloService.hello(name);
    }

    @GetMapping("/time")
    public String getServerTime() {
        logger.info("调用RPC服务: getServerTime()");
        return helloService.getServerTime();
    }
}
