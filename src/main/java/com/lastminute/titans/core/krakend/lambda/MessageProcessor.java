package com.lastminute.titans.core.krakend.lambda;

import com.google.protobuf.Message;
import com.lastminute.titans.core.krakend.lambda.kafka.Producer;
import com.lastminute.titans.core.krakend.lambda.kafka.ProducerFactory;
import com.lastminute.titans.core.krakend.lambda.protobuf.ProtobufTransformer;
import com.lastminute.titans.core.krakend.lambda.protobuf.ProtobufTransformerFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MessageProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);
    public static final String KAFKA_BOOSTRAP_SERVERS = "kafka.boostrap.servers";

    public static final String REGISTRY_NAME = "registry.name";
    public static final String AWS_REGION = "aws.region";

    private final ProtobufTransformerFactory protobufTransformerFactory;

    private final ProducerFactory producerFactory;

    public MessageProcessor() {
        this.protobufTransformerFactory = new ProtobufTransformerFactory();
        this.producerFactory = new ProducerFactory();
    }

    protected MessageProcessor(ProtobufTransformerFactory protobufTransformerFactory, ProducerFactory producerFactory) {
        this.protobufTransformerFactory = protobufTransformerFactory;
        this.producerFactory = producerFactory;
    }

    public void handleRequest(Properties lambdaEnvsProperties, LambdaRequest lambdaRequest) throws Exception {
        LOGGER.info("handling message");
        Message protoMessage = this.getProtobufMessage(lambdaEnvsProperties,  lambdaRequest);
        LOGGER.info("proto created");
        com.lastminute.titans.core.krakend.lambda.kafka.Message kafkaMessage = getKafkaMessage(protoMessage,lambdaRequest);
        LOGGER.info("kafka message created {}",kafkaMessage);
        Producer kafkaProducer = this.producerFactory.getKafkaProducer(lambdaEnvsProperties.getProperty(KAFKA_BOOSTRAP_SERVERS));
        LOGGER.info("kafka producer created");
        kafkaProducer.produceMessage(kafkaMessage);
        LOGGER.info("message sent");
    }

    private  Message getProtobufMessage(Properties lambdaEnvsProperties,LambdaRequest lambdaRequest) throws Exception{
        ProtobufTransformer serde = getSerdeInstance(lambdaEnvsProperties,lambdaRequest.getTopicName());
        JSONObject bodyInJson = new JSONObject(lambdaRequest.getBody());
        return serde.protoFromJson(bodyInJson.toString());
    }

    private  ProtobufTransformer getSerdeInstance(Properties lambdaEnvsProperties,String topicName){
       Properties serdeProps = new Properties();
       serdeProps.setProperty(ProtobufTransformerFactory.SERDE_NAME_KEY,"glue");
       serdeProps.setProperty(ProtobufTransformerFactory.REGION,lambdaEnvsProperties.getProperty(AWS_REGION));
       serdeProps.setProperty(ProtobufTransformerFactory.SCHEMA_NAME,topicName);
       serdeProps.setProperty(ProtobufTransformerFactory.SCHEMA_REGISTRY_NAME, lambdaEnvsProperties.getProperty(REGISTRY_NAME));
       return this.protobufTransformerFactory.getSerdeInstance(serdeProps);
    }

    private static com.lastminute.titans.core.krakend.lambda.kafka.Message getKafkaMessage(Message protoMessage, LambdaRequest lambdaRequest){
        return com.lastminute.titans.core.krakend.lambda.kafka.Message.builder().topic(lambdaRequest.getTopicName()).recordMessage(protoMessage).recordKey(lambdaRequest.getKey()).headers(transformHeadersIntoKafkaHeaders(lambdaRequest)).build();
    }

    private static List<Map<String,byte[]>> transformHeadersIntoKafkaHeaders(LambdaRequest lambdaRequest){
        Map<String, String> headers = lambdaRequest.getHeaders();
        Map<String,byte[]> kafkaHeaders = new HashMap<>();
        headers.forEach((key, value) -> kafkaHeaders.put(key, value.getBytes()));
        return List.of(kafkaHeaders);
    }


}
