package com.lastminute.titans.core.krakend.lambda.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ProducerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerFactory.class);

    public Producer getKafkaProducer(String boostrapServers){
        Map<String,Object> producerProps = setupKafkaProducerProps(boostrapServers);
        return new Producer(producerProps);
    }

    private static Map<String,Object> setupKafkaProducerProps(String boostrapServers) {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CustomSerializer.class.getName());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServers);

        setupMSKIAMProperties(props);
        LOGGER.info("producer properties in factory {}",props);

        return props;
    }



    private static void setupMSKIAMProperties(Map<String, Object> props) {
        props.put("security.protocol","SASL_SSL");
        props.put("sasl.mechanism","AWS_MSK_IAM");
        props.put("sasl.jaas.config","software.amazon.msk.auth.iam.IAMLoginModule required;");
        props.put("sasl.client.callback.handler.class","software.amazon.msk.auth.iam.IAMClientCallbackHandler");
    }
}
