package com.lastminute.titans.core.krakend.lambda.protobuf;

import com.lastminute.titans.core.krakend.lambda.protobuf.glue.GlueProtobufTransformer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.glue.GlueClient;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ProtobufTransformerFactoryTest {

    @Test
    void testGetSerdeInstanceInvalidSerdeName(){
        Properties propForTest = new Properties();
        propForTest.put(ProtobufTransformerFactory.SERDE_NAME_KEY,"invalid");
        executeGetSerdeInstanceThrowingIlegalArgumentException(propForTest);
    }

    @Test
    void testGetSerdeInstanceNullSerdeName(){
        Properties propForTest = new Properties();
        executeGetSerdeInstanceThrowingIlegalArgumentException(propForTest);
    }

    @Test
    void testGetSerdeInstanceInvalidGluePropertiesMissingSchema() {
        Properties propForTest = new Properties();
        propForTest.put(ProtobufTransformerFactory.SERDE_NAME_KEY,"glue");
        propForTest.put(ProtobufTransformerFactory.SCHEMA_REGISTRY_NAME,"myRegistry");
        propForTest.put(ProtobufTransformerFactory.REGION,"eu-central-1");

        executeGetSerdeInstanceThrowingIlegalArgumentException(propForTest);

    }

    @Test
    void testGetSerdeInstanceInvalidGluePropertiesMissingRegistryName() {
        Properties propForTest = new Properties();
        propForTest.put(ProtobufTransformerFactory.SERDE_NAME_KEY,"glue");
        propForTest.put(ProtobufTransformerFactory.SCHEMA_NAME,"mySchema");
        propForTest.put(ProtobufTransformerFactory.REGION,"eu-central-1");

        executeGetSerdeInstanceThrowingIlegalArgumentException(propForTest);

    }

    private static void executeGetSerdeInstanceThrowingIlegalArgumentException(Properties propForTest) {
        ProtobufTransformerFactory transformerFactory = new ProtobufTransformerFactory();
        assertThrows(IllegalArgumentException.class, ()->transformerFactory.getSerdeInstance(propForTest));
    }
}