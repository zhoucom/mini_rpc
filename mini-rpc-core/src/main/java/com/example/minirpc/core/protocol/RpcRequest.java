package com.example.minirpc.core.protocol;

import java.io.Serializable;

/**
 * RPC请求对象
 */
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 请求ID，用于匹配请求和响应
     */
    private String requestId;

    /**
     * 接口名称，即服务名
     */
    private String interfaceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 方法参数类型数组
     */
    private Class<?>[] parameterTypes;

    /**
     * 方法参数值数组
     */
    private Object[] parameters;
    
    /**
     * 默认构造函数
     */
    public RpcRequest() {
    }
    
    /**
     * 全参数构造函数
     */
    public RpcRequest(String requestId, String interfaceName, String methodName, 
                     Class<?>[] parameterTypes, Object[] parameters) {
        this.requestId = requestId;
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameters = parameters;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
