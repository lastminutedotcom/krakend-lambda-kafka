package com.lastminute.titans.core.krakend.lambda.kafka;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;
@Data
@Builder
@ToString
@EqualsAndHashCode
public class Message {

    private String topic;
    private Object recordKey;
    private com.google.protobuf.Message recordMessage;
    private List<Map<String, byte[]>> headers;
}
