package com.example.minirpc.starter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Mini RPC框架配置属性
 */
@ConfigurationProperties(prefix = "mini.rpc")
public class MiniRpcProperties {

    /**
     * 是否启用RPC服务器
     */
    private boolean serverEnable = false;
    
    /**
     * 是否启用RPC客户端
     */
    private boolean clientEnable = false;

    /**
     * 直连地址，格式为 host:port
     * 当设置此项时，将使用直连方式而不是服务发现
     */
    private String directAddress;
    
    /**
     * 服务注册与发现配置
     */
    private Registry registry = new Registry();
    
    /**
     * 服务端配置
     */
    private Server server = new Server();
    
    /**
     * 客户端配置
     */
    private Client client = new Client();
    
    /**
     * 服务注册配置
     */
    public static class Registry {
        /**
         * 注册中心类型，默认为local
         */
        private String type = "local";

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
    
    /**
     * 服务端配置
     */
    public static class Server {
        /**
         * 服务器主机名，默认为localhost
         */
        private String host = "localhost";
        
        /**
         * 服务器端口，默认为8088
         */
        private int port = 8088;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
    
    /**
     * 客户端配置
     */
    public static class Client {
        /**
         * 默认超时时间，单位毫秒
         */
        private long timeout = 5000;

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }
    }

    public boolean isServerEnable() {
        return serverEnable;
    }

    public void setServerEnable(boolean serverEnable) {
        this.serverEnable = serverEnable;
    }

    public boolean isClientEnable() {
        return clientEnable;
    }

    public void setClientEnable(boolean clientEnable) {
        this.clientEnable = clientEnable;
    }

    public String getDirectAddress() {
        return directAddress;
    }

    public void setDirectAddress(String directAddress) {
        this.directAddress = directAddress;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
