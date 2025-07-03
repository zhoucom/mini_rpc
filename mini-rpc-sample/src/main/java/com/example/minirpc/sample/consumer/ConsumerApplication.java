package com.example.minirpc.sample.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RPC服务消费者应用
 */
@SpringBootApplication
public class ConsumerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
