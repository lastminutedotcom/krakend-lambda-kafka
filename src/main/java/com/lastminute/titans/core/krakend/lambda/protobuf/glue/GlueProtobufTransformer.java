package com.lastminute.titans.core.krakend.lambda.protobuf.glue;

import com.lastminute.titans.core.krakend.lambda.protobuf.ProtobufTransformer;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glue.GlueClient;

public class GlueProtobufTransformer extends ProtobufTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlueProtobufTransformer.class);
    private GlueClient glueClient;

    private final SchemaHandler schemaHandler;

    private  AwsCredentialsProvider awsCredentialsProvider;


    public GlueProtobufTransformer(String region, String registryName, String schemaName) {

        this.awsCredentialsProvider = DefaultCredentialsProvider.create();
        this.configureGlueClient(region);
        this.schemaHandler = new SchemaHandler(glueClient, registryName, schemaName);
    }

    protected GlueProtobufTransformer(SchemaHandler schemaHandler){
        this.schemaHandler = schemaHandler;
    }
    @Override
    protected ProtobufSchema getProtoSchema() {
       String schemaDefinition = this.schemaHandler.getSchemaDefinition();
        LOGGER.info("Schema {}",schemaDefinition);
        return new ProtobufSchema(schemaDefinition);
    }
    private void configureGlueClient( String region) {
        this.glueClient = GlueClient.builder()
                .region(Region.of(region))
                .endpointOverride(null)
                .credentialsProvider(this.awsCredentialsProvider)
                .httpClient(ApacheHttpClient.create())
                .build();
    }

}


