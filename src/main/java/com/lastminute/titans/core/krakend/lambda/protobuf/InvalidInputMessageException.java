package com.lastminute.titans.core.krakend.lambda.protobuf;

public class InvalidInputMessageException extends Exception{



    public InvalidInputMessageException(String message) {
        super(message);
    }

    public static String generateErrorMessage(String inputJson, String schemaName){
        return String.format("Can not serialize input json %s using schema %s",inputJson,schemaName);
    }
}
