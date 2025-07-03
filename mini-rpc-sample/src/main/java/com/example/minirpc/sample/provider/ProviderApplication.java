package com.example.minirpc.sample.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * RPC服务提供者应用
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.minirpc.sample"})
public class ProviderApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
