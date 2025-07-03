package com.example.minirpc.core.protocol;

import java.io.Serializable;

/**
 * RPC响应对象
 */
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 请求ID，用于匹配请求和响应
     */
    private String requestId;

    /**
     * 响应状态码，0表示成功，非0表示失败
     */
    private Integer code;

    /**
     * 错误信息，成功时为null
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 默认构造函数
     */
    public RpcResponse() {
    }
    
    /**
     * 全参数构造函数
     */
    public RpcResponse(String requestId, Integer code, String message, T data) {
        this.requestId = requestId;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 创建成功响应
     */
    public static <T> RpcResponse<T> success(String requestId, T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setCode(0);
        response.setData(data);
        return response;
    }

    /**
     * 创建失败响应
     */
    public static <T> RpcResponse<T> fail(String requestId, Integer code, String message) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
