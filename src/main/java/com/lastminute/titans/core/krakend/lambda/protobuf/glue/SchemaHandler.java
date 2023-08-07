package com.lastminute.titans.core.krakend.lambda.protobuf.glue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.*;


public class SchemaHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaHandler.class);

    private final GlueClient glueClient;
    private final String registryName;
    private final String schemaName;

    private final SchemaId schemaId;

    private final GetSchemaResponse schemaResponse;

    public SchemaHandler(GlueClient glueClient, String registryName, String schemaName) {
        this.glueClient = glueClient;
        this.registryName = registryName;
        this.schemaName = schemaName;
        this.schemaId = this.getSchemaID();
        this.schemaResponse = this.getSchemaResponse();
    }
    private SchemaId getSchemaID(){
        return SchemaId.builder().registryName(this.registryName).schemaName(this.schemaName).build();
    }

    private GetSchemaResponse getSchemaResponse() {
        try {
            return this.glueClient.getSchema(
                            GetSchemaRequest.builder()
                                    .schemaId(this.schemaId)
                                    .build());
        } catch (EntityNotFoundException nfe) {
            LOGGER.error("Can not obtain the schema response, entity not found",nfe);
            throw  new RuntimeException("Error while trying to obtain the schema",nfe);
        }
    }

    public String getSchemaDefinition(){
        GetSchemaVersionResponse schemaVersionResponse = this.getGetSchemaVersionResponse(this.schemaResponse);
        return schemaVersionResponse.schemaDefinition();
    }
    private GetSchemaVersionResponse getGetSchemaVersionResponse(GetSchemaResponse schemaResponse) {
        try {
                return this.glueClient.getSchemaVersion(getSchemaVersionRequest(schemaResponse));
        } catch (EntityNotFoundException nfe) {
            LOGGER.error("Can not obtain the schema version response, entity not found",nfe);
            throw new RuntimeException("Error while trying to obtain the schema version",nfe);
        }
    }


    private GetSchemaVersionRequest getSchemaVersionRequest(GetSchemaResponse schemaResponse){
        return GetSchemaVersionRequest.builder()
                .schemaId(this.schemaId)
                .schemaVersionNumber(getSchemaVersionNumber(schemaResponse))
                .build();
    }

    private static SchemaVersionNumber getSchemaVersionNumber(GetSchemaResponse schemaResponse) {
        return SchemaVersionNumber.builder().versionNumber(schemaResponse.latestSchemaVersion()).build();
    }

}
