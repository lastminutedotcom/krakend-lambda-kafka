package com.lastminute.titans.core.krakend.lambda;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LambdaResponseTest {
    
    private static final String STATUS_CODE = "statusCode";
    
    private static final String MESSAGE_KEY = "message";

    @Test
    void testAsMapFull() {
        String expectedCode = "200";
        String expectedMessage = "test message";
        LambdaResponse responseForTest =  LambdaResponse.builder().statusCode(expectedCode).message(expectedMessage).build();
        Map<String,Object> expectedResponseMap = responseForTest.asMap();
        assertGeneratedMap(expectedCode, expectedMessage, expectedResponseMap);
    }

    private static void assertGeneratedMap(String expectedCode, String expectedMessage, Map<String, Object> expectedResponseMap) {
        assertNotNull(expectedResponseMap);
        assertTrue(expectedResponseMap.containsKey(STATUS_CODE));
        assertTrue(expectedResponseMap.containsKey(MESSAGE_KEY));
        assertEquals(expectedCode, expectedResponseMap.get(STATUS_CODE));
        assertEquals(expectedMessage, expectedResponseMap.get(MESSAGE_KEY));
    }
    @Test
    void testAsMapOnlyCode() {
        String expectedCode = "200";

        LambdaResponse responseForTest =  LambdaResponse.builder().statusCode(expectedCode).build();
        Map<String,Object> expectedResponseMap = responseForTest.asMap();
        assertGeneratedMapOnlyCode(expectedCode, expectedResponseMap);
    }

    private static void assertGeneratedMapOnlyCode(String expectedCode, Map<String, Object> expectedResponseMap) {
        assertNotNull(expectedResponseMap);
        assertTrue(expectedResponseMap.containsKey(STATUS_CODE));
        assertFalse(expectedResponseMap.containsKey(MESSAGE_KEY));
        assertEquals(expectedCode, expectedResponseMap.get(STATUS_CODE));
    }

    @Test
    void testAsMapOnlyMessage() {

        String expectedMessage = "test message";
        LambdaResponse responseForTest =  LambdaResponse.builder().message(expectedMessage).build();
        Map<String,Object> expectedResponseMap = responseForTest.asMap();
        assertGeneratedResonseOnlyMessage(expectedMessage, expectedResponseMap);

    }

    private static void assertGeneratedResonseOnlyMessage(String expectedMessage, Map<String, Object> expectedResponseMap) {
        assertNotNull(expectedResponseMap);
        assertFalse(expectedResponseMap.containsKey(STATUS_CODE));
        assertTrue(expectedResponseMap.containsKey(MESSAGE_KEY));
        assertEquals(expectedMessage, expectedResponseMap.get(MESSAGE_KEY));
    }

    @Test
    void testAsMapEmpty() {

        LambdaResponse responseForTest =  LambdaResponse.builder().build();
        Map<String,Object> expectedResponseMap = responseForTest.asMap();
        assertNotNull(expectedResponseMap);
        assertFalse(expectedResponseMap.containsKey(STATUS_CODE));
        assertFalse(expectedResponseMap.containsKey(MESSAGE_KEY));
    }

}