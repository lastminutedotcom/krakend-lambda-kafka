package com.lastminute.titans.core.krakend.lambda;

import lombok.*;

import java.util.Map;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class LambdaRequest {
    private Map<String,String> headers ;
    private Map<String,Object> body;
    private String topicName;
    private String key;
}
