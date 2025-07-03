package com.example.minirpc.core.client;

import com.example.minirpc.core.protocol.RpcRequest;
import com.example.minirpc.core.protocol.RpcResponse;

/**
 * RPC客户端接口
 */
public interface RpcClient {
    
    /**
     * 发送RPC请求并获取响应
     * 
     * @param request RPC请求对象
     * @return RPC响应对象
     */
    RpcResponse<?> send(RpcRequest request);
    
    /**
     * 关闭客户端
     */
    void close();
}
