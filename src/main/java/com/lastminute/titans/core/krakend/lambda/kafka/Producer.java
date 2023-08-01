package com.lastminute.titans.core.krakend.lambda.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Producer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
    private final KafkaProducer<Object,com.google.protobuf.Message> kafkaProducer;

    public Producer(Map<String,?> handlerProps) {
        Map<String, Object> producerProps = new HashMap<>(handlerProps);
        LOGGER.info("producer properties:{}",producerProps);
        this.kafkaProducer = new KafkaProducer<>(producerProps);
    }

    protected Producer(KafkaProducer<Object, com.google.protobuf.Message> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public void produceMessage(Message message){
        LOGGER.info("Processing message: {}",message.getRecordMessage());
        ProducerRecord<Object,com.google.protobuf.Message>  kafkaRecord= new ProducerRecord<>(message.getTopic(),message.getRecordKey(),message.getRecordMessage());
        setupHeaders(kafkaRecord.headers(),message.getHeaders());
        LOGGER.info("sending message");
        this.kafkaProducer.send(kafkaRecord);
        LOGGER.info("message sent by producer");
        this.kafkaProducer.flush();
        LOGGER.info("producer flushed");
        this.kafkaProducer.close();
        LOGGER.info("Message processed: {}",message.getRecordMessage());
    }

    private static void setupHeaders(Headers headers, List<Map<String,byte[]>> headersMap){
        if(Objects.nonNull(headersMap) && !headersMap.isEmpty()) {
            LOGGER.info("setting headers: {} into object {}", headersMap,headers);
            headersMap.forEach(map -> map.forEach(headers::add));
        }
    }


}
