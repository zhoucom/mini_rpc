# Mini RPC Framework

一个简单的RPC框架，打包为Spring Boot Starter，方便在Spring Boot应用中快速集成。

## 项目结构

- `mini-rpc-core`: 核心模块，提供RPC基础功能
- `mini-rpc-spring-boot-starter`: Spring Boot Starter，提供自动配置
- `mini-rpc-sample`: 示例项目，演示如何使用这个RPC框架

## 功能特性

- 基于Netty的高性能网络通信
- 基于Spring Boot的自动配置
- 使用注解方便地暴露和引用服务
- JSON序列化支持
- 简单的本地服务注册与发现

## 快速开始

### 1. 添加依赖

在您的Spring Boot项目中添加以下依赖：

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>mini-rpc-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 服务提供方

#### 定义服务接口

```java
public interface HelloService {
    String hello(String name);
    String getServerTime();
}
```

#### 实现服务接口并使用`@RpcService`注解暴露服务

```java
@RpcService(serviceInterface = HelloService.class)
public class HelloServiceImpl implements HelloService {
    
    @Override
    public String hello(String name) {
        return "Hello, " + name + "! Welcome to Mini RPC!";
    }
    
    @Override
    public String getServerTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "Server time: " + formatter.format(new Date());
    }
}
```

#### 配置服务提供方

在`application.yml`中配置：

```yaml
mini:
  rpc:
    server-enable: true
    server:
      host: localhost  # RPC服务器主机名
      port: 9090       # RPC服务器端口
```

### 3. 服务消费方

#### 使用`@RpcReference`注解引用远程服务

```java
@RestController
@RequestMapping("/hello")
public class HelloController {

    @RpcReference
    private HelloService helloService;

    @GetMapping("/say/{name}")
    public String sayHello(@PathVariable("name") String name) {
        return helloService.hello(name);
    }

    @GetMapping("/time")
    public String getServerTime() {
        return helloService.getServerTime();
    }
}
```

#### 配置服务消费方

在`application.yml`中配置：

```yaml
mini:
  rpc:
    client-enable: true
```

## 配置说明

### 服务器配置

```yaml
mini:
  rpc:
    server-enable: true  # 启用RPC服务器
    server:
      host: localhost    # RPC服务器主机名
      port: 9090         # RPC服务器端口
```

### 客户端配置

```yaml
mini:
  rpc:
    client-enable: true  # 启用RPC客户端
    client:
      timeout: 5000      # 请求超时时间（毫秒）
```

## 示例项目运行步骤

1. 克隆项目
2. 编译安装：`mvn clean install`

### 启动服务提供方

```bash
cd mini-rpc-sample
mvn spring-boot:run -Dspring-boot.run.profiles=provider
```

服务提供方将启动在`8081`端口。

### 启动服务消费方

```bash
cd mini-rpc-sample
mvn spring-boot:run -Dspring-boot.run.profiles=consumer
```

服务消费方将启动在`8082`端口。

### 测试服务

访问以下URL测试RPC调用：

- `http://localhost:8082/hello/say/world`
- `http://localhost:8082/hello/time`

## 扩展点

- 实现新的序列化方式：实现`Serializer`接口
- 实现新的服务注册与发现：实现`ServiceRegistry`接口

## 局限性和改进方向

当前实现是一个最小可用版本，有以下局限性：

1. 仅支持同步调用，不支持异步调用
2. 仅支持本地服务注册，不支持分布式服务注册（如ZooKeeper、Nacos等）
3. 没有服务治理功能（如负载均衡、熔断、限流等）
4. 没有考虑安全性（如身份验证和授权）

## 许可证

MIT
