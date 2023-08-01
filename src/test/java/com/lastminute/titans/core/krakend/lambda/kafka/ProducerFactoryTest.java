package com.lastminute.titans.core.krakend.lambda.kafka;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProducerFactoryTest {

    @Test
    void testGetKafkaProducerReturnsProducer(){
        ProducerFactory producerFactorySUT = new ProducerFactory();
        Producer actualProducer = producerFactorySUT.getKafkaProducer("http://localhost:8999");
        assertNotNull(actualProducer);
    }

}