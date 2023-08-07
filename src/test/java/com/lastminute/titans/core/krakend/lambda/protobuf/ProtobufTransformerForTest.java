package com.lastminute.titans.core.krakend.lambda.protobuf;

import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;

public class ProtobufTransformerForTest extends ProtobufTransformer{



    public static String PROTO_MESSAGE_FIELD_NAME = "some_message";

    public static String PROTO_MESSAGE_FIELD_EXAMPLE_VALUE = "test value";


    public static final String JSON_MESSAGE_FOR_TEST = "{ \""+PROTO_MESSAGE_FIELD_NAME+"\":\""+PROTO_MESSAGE_FIELD_EXAMPLE_VALUE+"\"}";

    private static final String PROTO_SCHEMA_AS_STRING = "" +
            "syntax = \"proto3\";\n" +
            "package poc-ttn-602.events;\n" +
            "option java_package = \"com.lastminute.poc-ttn-602.events\";\n" +
            "\n" +
            "message SampleEvent {\n" +
            "  string some_message = 1; // A generic field of the event of string type\n" +
            "}";

    @Override
    protected ProtobufSchema getProtoSchema() {
        return new ProtobufSchema(PROTO_SCHEMA_AS_STRING);
    }
}
