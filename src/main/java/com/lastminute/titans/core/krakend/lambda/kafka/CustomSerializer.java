package com.lastminute.titans.core.krakend.lambda.kafka;

import com.google.protobuf.Message;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

public class CustomSerializer implements Serializer<Message> {
    @Override
    public byte[] serialize(String s, Message message) {
        return message.toByteArray();
    }

    @Override
    public byte[] serialize(String topic, Headers headers, Message data) {
        return Serializer.super.serialize(topic, headers, data);
    }
}
