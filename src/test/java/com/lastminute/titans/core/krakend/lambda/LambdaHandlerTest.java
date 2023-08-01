package com.lastminute.titans.core.krakend.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.lastminute.titans.core.krakend.lambda.protobuf.InvalidInputMessageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LambdaHandlerTest {

    private static final LambdaRequest FULL_VALID_LAMBDAREQUEST =  new LambdaRequest(Map.of("header","value"),Map.of("test","value"),"topicTest","keyTest");

    @Mock
    private  MessageProcessor processorMock;

    @Mock
    private Context contextMock;

    @BeforeEach
    public void beforeEach(){
        when(this.contextMock.getAwsRequestId()).thenReturn("594593");
    }


    @Test
    void handleRequestProduce200Response() throws Exception {

        Properties lambdaEnvPropsForTest = setupSystemEnv();
        testHandleRequestProduce200Response(FULL_VALID_LAMBDAREQUEST, lambdaEnvPropsForTest);

    }

    @Test
    void handleRequestProduce200ResponseNoHeaders() throws Exception {

        LambdaRequest requestForTest = new LambdaRequest(Collections.emptyMap(),Map.of("test","value"),"topicTest","keyTest");
        Properties lambdaEnvPropsForTest = setupSystemEnv();

        testHandleRequestProduce200Response(requestForTest, lambdaEnvPropsForTest);

    }

    @Test
    void handleRequestProduce200ResponseNoKey() throws Exception {

        LambdaRequest requestForTest = new LambdaRequest(Collections.emptyMap(),Map.of("test","value"),"topicTest",null);
        Properties lambdaEnvPropsForTest = setupSystemEnv();

        testHandleRequestProduce200Response(requestForTest, lambdaEnvPropsForTest);

    }

    private void testHandleRequestProduce200Response(LambdaRequest requestForTest, Properties lambdaEnvPropsForTest) throws Exception {
        doNothing().when(this.processorMock).handleRequest(lambdaEnvPropsForTest, requestForTest);

        Map<String,Object>  response = executeHandleRequestInSut(requestForTest, lambdaEnvPropsForTest);
        assertResponse200(response);
        verify(this.processorMock).handleRequest(lambdaEnvPropsForTest, requestForTest);
    }

    private static void assertResponse200(Map<String, Object> response) {
        verifyResponseCode(response, "200");
        assertEquals("ok", response.get("message"));
    }


    @Test
    void handleRequestProduce400ResponseNoBody() throws Exception {
        LambdaRequest requestForTest = new LambdaRequest(Map.of("header","value"),Collections.emptyMap(),"topicTest","keyTest");
        Properties lambdaEnvPropsForTest = setupSystemEnv();
        testHandleRequestProduce400RequiredInputs(requestForTest, lambdaEnvPropsForTest);
    }
    @Test
    void handleRequestProduce400ResponseNoTopicName() throws Exception {
        LambdaRequest requestForTest = new LambdaRequest(Map.of("header","value"),Map.of("test","value"),null,"keyTest");
        Properties lambdaEnvPropsForTest = setupSystemEnv();
        testHandleRequestProduce400RequiredInputs(requestForTest, lambdaEnvPropsForTest);
    }

    private void testHandleRequestProduce400RequiredInputs(LambdaRequest requestForTest, Properties lambdaEnvPropsForTest) throws Exception {
        Map<String,Object>  response = executeHandleRequestInSut(requestForTest, lambdaEnvPropsForTest);
        verifyResponseCode(response,"400");
        verify(this.processorMock,never()).handleRequest(any(Properties.class),any(LambdaRequest.class));
    }

    @Test
    void handleRequestProduce400ResponseSchemaError() throws Exception {
        Properties lambdaEnvPropsForTest = setupSystemEnv();
        doThrow(new InvalidInputMessageException("test message")).when(this.processorMock).handleRequest(lambdaEnvPropsForTest, FULL_VALID_LAMBDAREQUEST);
        Map<String,Object>  response = executeHandleRequestInSut(FULL_VALID_LAMBDAREQUEST, lambdaEnvPropsForTest);
        verifyResponseCode(response,"400");
        verify(this.processorMock).handleRequest(any(Properties.class),any(LambdaRequest.class));
    }

    @Test
    void handleRequestProduce500() throws Exception {
        Properties lambdaEnvPropsForTest = setupSystemEnv();
        doThrow(RuntimeException.class).when(this.processorMock).handleRequest(lambdaEnvPropsForTest, FULL_VALID_LAMBDAREQUEST);
        Map<String,Object>  response = executeHandleRequestInSut(FULL_VALID_LAMBDAREQUEST, lambdaEnvPropsForTest);
        verifyResponseCode(response,"500");
        verify(this.processorMock).handleRequest(any(Properties.class),any(LambdaRequest.class));
    }

    private Map<String,Object>  executeHandleRequestInSut(LambdaRequest requestForTest, Properties lambdaEnvPropsForTest) {
        LambdaHandler lambdaHandlerSUT = new LambdaHandler(this.processorMock, lambdaEnvPropsForTest);

        return  lambdaHandlerSUT.handleRequest(requestForTest,this.contextMock);
    }




    private static void verifyResponseCode(Map<String, Object> response, String expected) {
        assertBasicResponse(response);
        assertEquals(expected, response.get("statusCode"));
    }

    private static void assertBasicResponse(Map<String, Object> response) {
        assertNotNull(response);
        assertTrue(response.containsKey("statusCode"));
        assertTrue(response.containsKey("message"));
    }

    private static Properties setupSystemEnv(){
        Properties props = new Properties();
        props.put(LambdaHandler.AWS_REGION,"eu-central-1");
        props.put(LambdaHandler.BOOSTRAP_SERVERS,"http://localhost:9090");
        props.put(LambdaHandler.GLUE_REGISTRY_NAME,"fakeRegistry");
        return props;
    }

}
