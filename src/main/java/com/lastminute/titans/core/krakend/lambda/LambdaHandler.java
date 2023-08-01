package com.lastminute.titans.core.krakend.lambda;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.common.base.Strings;
import com.lastminute.titans.core.krakend.lambda.protobuf.InvalidInputMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class LambdaHandler implements RequestHandler<LambdaRequest,Map<String,Object>> {

    public static final String AWS_REGION = "REGION";
    public static final String GLUE_REGISTRY_NAME = "GLUE_REGISTRY_NAME";

    public static final String BOOSTRAP_SERVERS = "KAFKA_BOOSTRAP_SERVERS";
    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaHandler.class);
    private final MessageProcessor processor;

    private final Properties systemProps;

    public LambdaHandler() {
        this.processor = new MessageProcessor();
        this.systemProps = generateEnvProperties();
    }

    private static Properties generateEnvProperties(){
        Properties props = new Properties();
        props.putAll(Map.of(MessageProcessor.KAFKA_BOOSTRAP_SERVERS,System.getenv(BOOSTRAP_SERVERS),MessageProcessor.AWS_REGION, System.getenv(AWS_REGION),MessageProcessor.REGISTRY_NAME, System.getenv(GLUE_REGISTRY_NAME)));
        return props;
    }
    protected LambdaHandler(MessageProcessor processor,Properties props ) {
        this.processor = processor;
        this.systemProps = props;
    }
    @Override
    public Map<String,Object> handleRequest(LambdaRequest lambdaRequest, Context context) {
        LOGGER.info("Processing Request ID {} with {}  \n",context.getAwsRequestId(), lambdaRequest);
        LambdaResponse response;
        if(validateNotEmptyBody(lambdaRequest) && validateTopicNameIsPresent(lambdaRequest)){
            response =   processMessage(lambdaRequest);
        }else{
            response =   LambdaResponse.builder().statusCode("400").message("body and topicName can not be empty or null").build();
        }
        LOGGER.info("Lambda response as map: {}",response.asMap());
        return response.asMap();
    }

    private  LambdaResponse processMessage(LambdaRequest request) {
        LambdaResponse response;
        try {

            this.processor.handleRequest(this.systemProps, request);
            response =  LambdaResponse.builder().statusCode("200").message("ok").build();
        } catch (Exception e) {
            response  = generateErrorResponse(e);
        }
        return response;
    }

    private static boolean validateNotEmptyBody(LambdaRequest request){
        boolean isValid = false;
        if(Objects.nonNull(request) && (Objects.nonNull(request.getBody()) && !request.getBody().isEmpty())){
            isValid = true;
        }
        return isValid;
    }

    private static boolean validateTopicNameIsPresent(LambdaRequest request){
        boolean isValid = false;
        if(Objects.nonNull(request) &&  !Strings.isNullOrEmpty(request.getTopicName())){
            isValid = true;
        }

        return isValid;
    }


    private static LambdaResponse generateErrorResponse(Throwable tw){
        LOGGER.error("Lambda returning error {}",tw.getMessage(),tw);
        LambdaResponse response;
        if(tw instanceof InvalidInputMessageException){
            response = LambdaResponse.builder().statusCode("400").message(tw.getMessage()).build();
        }else{
            response = LambdaResponse.builder().statusCode("500").message(String.format("Error message:",tw.getMessage())).build();
        }
       return response;
    }
}
