package com.lastminute.titans.core.krakend.lambda.protobuf.glue;

import com.lastminute.titans.core.krakend.lambda.kafka.Message;
import com.lastminute.titans.core.krakend.lambda.protobuf.ProtobufTransformer;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.glue.GlueClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlueProtobufTransformerTest {


    private static final String PROTO_SCHEMA_AS_STRING = "" +
            "syntax = \"proto3\";\n" +
            "package test;\n" +
            "\n" +
            "message TestProto {\n" +
            "  string message = 1; \n" +
            "}";

    @Mock
    private  SchemaHandler schemaHandlerMock;



    private GlueProtobufTransformer glueProtobufTransformerSUT;

    @BeforeEach
    void setUp() {
        this.glueProtobufTransformerSUT = new GlueProtobufTransformer(this.schemaHandlerMock);
    }

    @Test
    void testGetProtoSchema() {
        when(this.schemaHandlerMock.getSchemaDefinition()).thenReturn(PROTO_SCHEMA_AS_STRING);
        ProtobufSchema actualSchema = this.glueProtobufTransformerSUT.getProtoSchema();
        assertEquals(new ProtobufSchema(PROTO_SCHEMA_AS_STRING),actualSchema);
    }
    @Test
    void testGetProtoSchemaSchemaHandlerThrowsException() {
        doThrow(RuntimeException.class).when(this.schemaHandlerMock).getSchemaDefinition();
        assertThrows(RuntimeException.class, ()->this.glueProtobufTransformerSUT.getProtoSchema());
    }

}