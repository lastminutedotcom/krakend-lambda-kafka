package com.lastminute.titans.core.krakend.lambda.protobuf.glue;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class SchemaHandlerTest {

     private static final String PROTO_SCHEMA_AS_STRING = "" +
            "syntax = \"proto3\";\n" +
            "package test;\n" +
            "\n" +
            "message TestMessage {\n" +
            "  string message = 1; \n" +
            "}";
    @Mock
    private GlueClient glueClientMock;

    @Mock
    private GetSchemaResponse schemaResponseMock;

    @Mock
    private GetSchemaVersionResponse getSchemaVersionResponseMock;



    @Test
    void testGetSchemaDefinitionOk(){
        setupMocksForOk();
        SchemaHandler schemaHandlerSUT = new SchemaHandler(this.glueClientMock, "myRegistry", "mySchema");
        String actualSchema = schemaHandlerSUT.getSchemaDefinition();
        assertEquals(PROTO_SCHEMA_AS_STRING,actualSchema);
    }

    private void setupMocksForOk() {

        when(this.glueClientMock.getSchema(any(GetSchemaRequest.class))).thenReturn(this.schemaResponseMock);
        when(this.glueClientMock.getSchemaVersion(any(GetSchemaVersionRequest.class))).thenReturn(this.getSchemaVersionResponseMock);
        when(this.getSchemaVersionResponseMock.schemaDefinition()).thenReturn(PROTO_SCHEMA_AS_STRING);
    }

    @Test
    void testGetSchemaDefinitionGlueCLientGetSchemaThrowsException(){
        doThrow(EntityNotFoundException.class).when(this.glueClientMock).getSchema(any(GetSchemaRequest.class));
        assertThrows(RuntimeException.class, ()-> new SchemaHandler(this.glueClientMock, "myRegistry", "mySchema"));
    }

    @Test
    void testGetSchemaDefinitionGlueClientGetSchemaVersionThrowsException(){
        when(this.glueClientMock.getSchema(any(GetSchemaRequest.class))).thenReturn(this.schemaResponseMock);
        doThrow(EntityNotFoundException.class).when(this.glueClientMock).getSchemaVersion(any(GetSchemaVersionRequest.class));

        executeGetSchemaDefinitionThrowsRuntimeException();
    }

    @Test
    void testGetSchemaDefinitionGlueClientGetSchemaVersionResponseThrowsException(){
        when(this.glueClientMock.getSchema(any(GetSchemaRequest.class))).thenReturn(this.schemaResponseMock);
        when(this.glueClientMock.getSchemaVersion(any(GetSchemaVersionRequest.class))).thenReturn(this.getSchemaVersionResponseMock);
        doThrow(EntityNotFoundException.class).when(this.getSchemaVersionResponseMock).schemaDefinition();


        executeGetSchemaDefinitionThrowsRuntimeException();
    }

    private void executeGetSchemaDefinitionThrowsRuntimeException() {
        SchemaHandler schemaHandlerSUT = new SchemaHandler(this.glueClientMock, "myRegistry", "mySchema");
        assertThrows(RuntimeException.class, schemaHandlerSUT::getSchemaDefinition);
    }

}