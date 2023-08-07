package com.lastminute.titans.core.krakend.lambda;

import com.google.protobuf.Message;
import com.lastminute.titans.core.krakend.lambda.kafka.Producer;
import com.lastminute.titans.core.krakend.lambda.kafka.ProducerFactory;
import com.lastminute.titans.core.krakend.lambda.protobuf.InvalidInputMessageException;
import com.lastminute.titans.core.krakend.lambda.protobuf.ProtobufTransformer;
import com.lastminute.titans.core.krakend.lambda.protobuf.ProtobufTransformerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageProcessorTest {

    private static final LambdaRequest LAMBDAREQUEST_FOR_TEST =  new LambdaRequest(Map.of("header1","value","header2","value2"), Map.of("message_test","my_value"),"topicTest",null);
    @Mock
    private ProtobufTransformerFactory protobufTransformerFactoryMock;

    @Mock
    private ProducerFactory producerFactoryMock;

    @Mock
    private Message protoMessageMock;

    @Mock
    private ProtobufTransformer protobufTransformerMock;

    @Mock
    private Producer kafkaProducerMock;

    @Captor
    private ArgumentCaptor<com.lastminute.titans.core.krakend.lambda.kafka.Message> kafkaMessageCaptor;

    private MessageProcessor messageProcessorSUT;
    @BeforeEach
    void setUp() {
        this.messageProcessorSUT = new MessageProcessor(this.protobufTransformerFactoryMock,this.producerFactoryMock);
    }

    @Test
    void testHandleRequestMessageSent() throws Exception {
        Properties lambdaProperties = setupLambdaEnvForTest();
        setupMocksForMessageSending(lambdaProperties);

        this.messageProcessorSUT.handleRequest(lambdaProperties,LAMBDAREQUEST_FOR_TEST);

        verifyMocksCallsForMessageSent(lambdaProperties);
        assertSentMessage(this.kafkaMessageCaptor.getValue());
    }

    private void setupMocksForMessageSending(Properties lambdaProperties) throws InvalidInputMessageException {
        when(this.protobufTransformerFactoryMock.getSerdeInstance(any(Properties.class))).thenReturn(this.protobufTransformerMock);
        when(this.protobufTransformerMock.protoFromJson(any(String.class))).thenReturn(this.protoMessageMock);
        when(this.producerFactoryMock.getKafkaProducer(lambdaProperties.getProperty(MessageProcessor.KAFKA_BOOSTRAP_SERVERS))).thenReturn(this.kafkaProducerMock);
        doNothing().when(this.kafkaProducerMock).produceMessage(any(com.lastminute.titans.core.krakend.lambda.kafka.Message.class));
    }

    private void verifyMocksCallsForMessageSent(Properties lambdaProperties) throws InvalidInputMessageException {
        verify(this.protobufTransformerFactoryMock).getSerdeInstance(any(Properties.class));
        verify(this.protobufTransformerMock).protoFromJson(any(String.class));
        verify(this.producerFactoryMock).getKafkaProducer(lambdaProperties.getProperty(MessageProcessor.KAFKA_BOOSTRAP_SERVERS));
        verify(this.kafkaProducerMock).produceMessage(this.kafkaMessageCaptor.capture());
    }

    private void assertSentMessage(com.lastminute.titans.core.krakend.lambda.kafka.Message actualMessage) {
        assertNotNull(actualMessage);
        assertEquals(LAMBDAREQUEST_FOR_TEST.getTopicName(), actualMessage.getTopic());
        assertEquals(LAMBDAREQUEST_FOR_TEST.getHeaders(),getKafkaHeadersAsMapOfStrings(actualMessage.getHeaders()));
        assertEquals(LAMBDAREQUEST_FOR_TEST.getKey(), actualMessage.getRecordKey());
        assertEquals(this.protoMessageMock, actualMessage.getRecordMessage());
    }


    private static Map<String,String> getKafkaHeadersAsMapOfStrings(List<Map<String, byte[]>> actualKafkaHeaders){
        Map<String,String> actualMapOfStrings = new HashMap<>();
        actualKafkaHeaders.forEach(map->map.entrySet().forEach(entry->actualMapOfStrings.put(entry.getKey(),new String(entry.getValue()))));
        return actualMapOfStrings;
    }

    @Test
    void testHandleRequestErrorCreatingProtoMessage() throws Exception {
        Properties lambdaProperties = setupLambdaEnvForTest();
        when(this.protobufTransformerFactoryMock.getSerdeInstance(any(Properties.class))).thenReturn(this.protobufTransformerMock);
        doThrow(RuntimeException.class).when(this.protobufTransformerMock).protoFromJson(any(String.class));

        assertThrows(RuntimeException.class,()->this.messageProcessorSUT.handleRequest(lambdaProperties,LAMBDAREQUEST_FOR_TEST));

        verifyMocksNotCalledWhenErrorOnProtoTransformer();
    }

    private void verifyMocksNotCalledWhenErrorOnProtoTransformer() throws InvalidInputMessageException {
        verify(this.protobufTransformerFactoryMock).getSerdeInstance(any(Properties.class));
        verify(this.protobufTransformerMock).protoFromJson(any(String.class));
        verify(this.producerFactoryMock,never()).getKafkaProducer(any(String.class));
        verify(this.kafkaProducerMock,never()).produceMessage(any(com.lastminute.titans.core.krakend.lambda.kafka.Message.class));
    }

    @Test
    void testHandleRequestErrorSendingMessage() throws Exception {
        Properties lambdaProperties = setupLambdaEnvForTest();
        setupMocksForErrorSendingMessage(lambdaProperties);

        assertThrows(RuntimeException.class,()->this.messageProcessorSUT.handleRequest(lambdaProperties,LAMBDAREQUEST_FOR_TEST));

        verifyMocksCallsForErrorSendingMessage(lambdaProperties);
    }

    private void setupMocksForErrorSendingMessage(Properties lambdaProperties) throws InvalidInputMessageException {
        when(this.protobufTransformerFactoryMock.getSerdeInstance(any(Properties.class))).thenReturn(this.protobufTransformerMock);
        when(this.protobufTransformerMock.protoFromJson(any(String.class))).thenReturn(this.protoMessageMock);
        when(this.producerFactoryMock.getKafkaProducer(lambdaProperties.getProperty(MessageProcessor.KAFKA_BOOSTRAP_SERVERS))).thenReturn(this.kafkaProducerMock);
        doThrow(RuntimeException.class).when(this.kafkaProducerMock).produceMessage(any(com.lastminute.titans.core.krakend.lambda.kafka.Message.class));


    }

    private void verifyMocksCallsForErrorSendingMessage(Properties lambdaProperties) throws InvalidInputMessageException {
        verify(this.protobufTransformerFactoryMock).getSerdeInstance(any(Properties.class));
        verify(this.protobufTransformerMock).protoFromJson(any(String.class));
        verify(this.producerFactoryMock).getKafkaProducer(lambdaProperties.getProperty(MessageProcessor.KAFKA_BOOSTRAP_SERVERS));
        verify(this.kafkaProducerMock).produceMessage(any(com.lastminute.titans.core.krakend.lambda.kafka.Message.class));
    }


    private static Properties setupLambdaEnvForTest(){
        Properties props = new Properties();
        props.put(MessageProcessor.AWS_REGION,"eu-central-1");
        props.put(MessageProcessor.KAFKA_BOOSTRAP_SERVERS,"http://localhost:9098");
        props.put(MessageProcessor.REGISTRY_NAME,"fakeRegistry");
        return props;
    }

}