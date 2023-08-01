package com.lastminute.titans.core.krakend.lambda.protobuf;


import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProtobufTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtobufTransformer.class);
  public Message protoFromJson(String json) throws InvalidInputMessageException {
      LOGGER.info("json received:{}",json);
      ProtobufSchema schema = this.getProtoSchema();
      DynamicMessage.Builder message = schema.newMessageBuilder();
        try {
            JsonFormat.parser().merge(json, message);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("An invalid protocol exception occur",e);
            handleInvalidProtocolBufferException(e,json,schema.name());
        }
        LOGGER.info("proto message created");
        return message.build();
    }

    abstract protected ProtobufSchema getProtoSchema();

    private static void handleInvalidProtocolBufferException(InvalidProtocolBufferException e,String json,String schemaName) throws InvalidInputMessageException {
        if(e.getMessage().contains("Cannot find field")){
            throw new InvalidInputMessageException(InvalidInputMessageException.generateErrorMessage(json,schemaName));
        }else{
            throw new RuntimeException(e);
        }
    }



}
