package com.lastminute.titans.core.krakend.lambda.protobuf;

import com.lastminute.titans.core.krakend.lambda.protobuf.glue.GlueProtobufTransformer;

import java.util.Properties;

public  class ProtobufTransformerFactory {

    public static final String SERDE_NAME_KEY = "serde.name";
    public static final String REGION = "region";

    public static final String SCHEMA_REGISTRY_NAME = "serde.schema.registry.name";

    public static final String SCHEMA_NAME = "serde.schema.name";

    public  ProtobufTransformer getSerdeInstance(Properties serdeProperties){
       ProtobufTransformer serde;
        if(serdeProperties.containsKey(SERDE_NAME_KEY) && serdeProperties.getProperty(SERDE_NAME_KEY).equalsIgnoreCase("glue")){
            validateMandatoryGlueProperties(serdeProperties);
            serde = new GlueProtobufTransformer(serdeProperties.getProperty(REGION),serdeProperties.getProperty(SCHEMA_REGISTRY_NAME),serdeProperties.getProperty(SCHEMA_NAME));
        }else{
            throw new IllegalArgumentException(String.format("Invalid serde %s",serdeProperties.getProperty(SERDE_NAME_KEY)));
        }
        return serde;
    }

    private static void validateMandatoryGlueProperties(Properties serdeProperties){
        if(!serdeProperties.containsKey(SCHEMA_NAME) ||!serdeProperties.containsKey(SCHEMA_REGISTRY_NAME)  ){
            throw new IllegalArgumentException(String.format("required properties %s,%s not found",SCHEMA_NAME,SCHEMA_REGISTRY_NAME));
        }
    }
}
