package com.example.minirpc.core.serialize;

/**
 * 序列化接口
 */
public interface Serializer {

    /**
     * 序列化对象为字节数组
     *
     * @param obj 要序列化的对象
     * @return 序列化后的字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化字节数组为对象
     *
     * @param bytes 序列化后的字节数组
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
