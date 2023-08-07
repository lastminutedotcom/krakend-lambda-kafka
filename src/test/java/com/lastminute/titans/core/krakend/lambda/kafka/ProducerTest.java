package com.lastminute.titans.core.krakend.lambda.kafka;

import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProducerTest {

    @Mock
    private KafkaProducer<Object, Message> kafkaProducerMock;

    @Mock
    private Message mockedProtoMessage;

    @Mock
    private Future futureMock;

    @Captor
    private ArgumentCaptor<ProducerRecord> producerRecordArgumentCaptor;


    private Producer producerSUT;

    @BeforeEach
    void setUp() {
        this.producerSUT = new Producer(kafkaProducerMock);
    }

    @Test
    void testProduceMessageNoHeadersOrKey() {
        com.lastminute.titans.core.krakend.lambda.kafka.Message expectedMessage = generateMessageForTest(null, null);
        executeProduceMessage(expectedMessage);
    }

    @Test
    void testProduceMessageNoHeaders() {
        com.lastminute.titans.core.krakend.lambda.kafka.Message expectedMessage = generateMessageForTest("myKey", null);
        executeProduceMessage(expectedMessage);
    }

    @Test
    void testProduceMessage() {
        com.lastminute.titans.core.krakend.lambda.kafka.Message expectedMessage = generateMessageForTest("myKey", getHeadersForTest());
        executeProduceMessage(expectedMessage);
    }

    private static List<Map<String, byte[]>> getHeadersForTest() {
        return List.of(Map.of("header1", "value1".getBytes(), "header2", "value2".getBytes()));
    }

    private void executeProduceMessage(com.lastminute.titans.core.krakend.lambda.kafka.Message expectedMessage) {
        when(this.kafkaProducerMock.send(any(ProducerRecord.class))).thenReturn(this.futureMock);
        this.producerSUT.produceMessage(expectedMessage);
        verify(this.kafkaProducerMock).send(this.producerRecordArgumentCaptor.capture());
        verifyProducerFlushedAndClose();
        assertMessageSent(expectedMessage);
    }

    private void verifyProducerFlushedAndClose() {
        verify(this.kafkaProducerMock).flush();
        verify(this.kafkaProducerMock).close();
    }

    private void assertMessageSent(com.lastminute.titans.core.krakend.lambda.kafka.Message expectedMessage) {
        ProducerRecord actualRecord = this.producerRecordArgumentCaptor.getValue();
        assertEquals(expectedMessage.getRecordMessage(), actualRecord.value());
        assertEquals(expectedMessage.getRecordKey(), actualRecord.key());
        assertHeaders(expectedMessage, actualRecord);
    }

    private static void assertHeaders(com.lastminute.titans.core.krakend.lambda.kafka.Message expectedMessage, ProducerRecord actualRecord) {
        if (Objects.isNull(expectedMessage.getHeaders())) {
            assertEquals(0,actualRecord.headers().toArray().length );
        } else {
            assertHeadersValues(expectedMessage, actualRecord);
        }
    }

    private static void assertHeadersValues(com.lastminute.titans.core.krakend.lambda.kafka.Message expectedMessage, ProducerRecord actualRecord) {
        Header[] headersActualArray = actualRecord.headers().toArray();
        Map<String, byte[]> expectedHeaders = expectedMessage.getHeaders().get(0);
        assertEquals(expectedHeaders.size(), headersActualArray.length);

        for (Header header : headersActualArray) {
            assertEquals(expectedHeaders.get(header.key()), header.value());
        }
    }

    @Test
    void testProduceMessageFlushThrowsException() {
        when(this.kafkaProducerMock.send(any(ProducerRecord.class))).thenReturn(this.futureMock);
        doThrow(RuntimeException.class).when(this.kafkaProducerMock).flush();

        assertThrows(RuntimeException.class, () -> this.producerSUT.produceMessage(generateMessageForTest(null,null)));

        verify(this.kafkaProducerMock).send(any(ProducerRecord.class));
        verify(this.kafkaProducerMock,never()).close();
    }
    @Test
    void testProduceMessageCloseThrowsException() {
        when(this.kafkaProducerMock.send(any(ProducerRecord.class))).thenReturn(this.futureMock);
        doThrow(RuntimeException.class).when(this.kafkaProducerMock).close();

        assertThrows(RuntimeException.class, () -> this.producerSUT.produceMessage(generateMessageForTest(null,null)));
        verify(this.kafkaProducerMock).send(any(ProducerRecord.class));
        verify(this.kafkaProducerMock).flush();
    }
    @Test
    void testProduceMessageSendThrowsException() {
        doThrow(RuntimeException.class).when(this.kafkaProducerMock).send(any(ProducerRecord.class));

        assertThrows(RuntimeException.class, () -> this.producerSUT.produceMessage(generateMessageForTest(null,null)));

        verify(this.kafkaProducerMock,never()).flush();
        verify(this.kafkaProducerMock,never()).close();
    }

    private com.lastminute.titans.core.krakend.lambda.kafka.Message generateMessageForTest(String key, List<Map<String, byte[]>> headers) {
        return com.lastminute.titans.core.krakend.lambda.kafka.Message.builder().topic("mytopic").recordKey(key).headers(headers).recordMessage(mockedProtoMessage).build();
    }

}