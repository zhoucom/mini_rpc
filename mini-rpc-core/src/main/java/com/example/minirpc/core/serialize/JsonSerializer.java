package com.example.minirpc.core.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于Jackson的JSON序列化实现
 */
public class JsonSerializer implements Serializer {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (Exception e) {
            logger.error("序列化对象时发生错误: {}", obj, e);
            throw new RuntimeException("序列化错误", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (Exception e) {
            logger.error("反序列化对象时发生错误, 类型: {}", clazz.getName(), e);
            throw new RuntimeException("反序列化错误", e);
        }
    }
}
