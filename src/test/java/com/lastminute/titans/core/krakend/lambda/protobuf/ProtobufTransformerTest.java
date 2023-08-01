package com.lastminute.titans.core.krakend.lambda.protobuf;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProtobufTransformerTest  {


    private ProtobufTransformer protobufTransformerSut;

    @BeforeEach
    void setUp() {

        this.protobufTransformerSut = new ProtobufTransformerForTest();

    }

    @Test
    void testProtoFromJsonGeneratesProto() throws InvalidInputMessageException {
        Message actualMessage = this.protobufTransformerSut.protoFromJson(ProtobufTransformerForTest.JSON_MESSAGE_FOR_TEST);

        Map<Descriptors.FieldDescriptor,Object> fields = actualMessage.getAllFields();

        assertNotNull(actualMessage);
        assertTrue(fields.keySet().stream().anyMatch(fieldDescriptor -> fieldDescriptor.getName().equals(ProtobufTransformerForTest.PROTO_MESSAGE_FIELD_NAME)));
        assertEquals(ProtobufTransformerForTest.PROTO_MESSAGE_FIELD_EXAMPLE_VALUE,(fields.get(fields.keySet().stream().filter(fieldDescriptor -> fieldDescriptor.getName().equals(ProtobufTransformerForTest.PROTO_MESSAGE_FIELD_NAME)).findFirst().get())));
    }

    @Test
    void testProtoFromJsonThrowsInvalidInputMessageException() throws InvalidInputMessageException {
        String invalidJsonMessage = "{\"other_key\":\"1\"}";
        assertThrows(InvalidInputMessageException.class,()->this.protobufTransformerSut.protoFromJson(invalidJsonMessage));
    }
}