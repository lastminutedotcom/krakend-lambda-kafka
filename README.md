# KrakenD-lambda-kafka

This project holds the code of a lambda in charge of receiving a json request,
transform it into a protobuf, extract headers and key if present and, publish it into a given kafka cluster.


## Deployment Configuration

* This lambda is intended to run **in the same VPC where the MSK cluster is**.
* The role used by this lambda should have attached policies that allow it to connect to the MSK cluster and publish messages in the topic
```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": [
                "kafka-cluster:Connect",
                "kafka-cluster:DescribeTopic",
                "kafka-cluster:WriteData"
            ],
            "Effect": "Allow",
            "Resource": [
                "arn:aws:kafka:eu-central-1:${account-id}:cluster/${cluster-name}/*",
                "arn:aws:kafka:eu-central-1:${account-id}:topic/${cluster-name}/*/${topic-name}"
            ]
        }
    ]
}
```
* Also, it is required to add a policy that allows to write data idempotently
```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": ["kafka-cluster:WriteDataIdempotently"],
            "Resource": ["arn:aws:kafka:*:${account-id}:cluster/poc-ttn-602-kafka-cluster/*" ]
        }
    ]
}
```
* In order to be allowed to connect to the cluster, is also required that the security group used by the lambda is allowed to
connect to the cluster ports, for doing this, go to the MSK cluster security group and add the lambda security group id as source.
* Also is expected that each topic has a schema published in AWS Glue in the same account where the VPC is,
this schema should have the same name as the topic. For accessing the registry, it is required to have the following policy attached:

```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": [
                "glue:GetSchemaVersionsDiff",
                "glue:GetSchema",
                "glue:GetSchemaByDefinition"
            ],
            "Effect": "Allow",
            "Resource": [
                "arn:aws:glue:eu-central-1:${account-id}:registry/${registry-name}",
                "arn:aws:glue:eu-central-1:${account-id}:schema/${registry-name}/*"
            ]
        },
        {
            "Action": [
                "glue:GetSchemaVersion"
            ],
            "Effect": "Allow",
            "Resource": [
                "*"
            ]
        }
    ]
}
```
### Deployment configuration using the Kafka Governance Process

If this lambda is going to publish messages on an MSK created using the Kafka Governance Process, the configuration is as follows:

For allowing the Lambda to publish messages and to access the Glue Registry:

1. Open the `terraform.tfvars` in the kafka governance process project and search for the `kafka_topics_configuration` section
2.  in the `roles_grants` add the role used by the lambda adding the `w` as grant, like this example:
```
 {
    role   = "krakend-lambda-kafka-role-op993qgu",
    grants = ["w"]
  }
```
3. Repeat the 2nd step for each topic required.

For allowing the lambda to establish a connection with the cluster:

1. Open the `terraform.tfvars` in the kafka governance process project and search for the `cluster_ingress` section
    
2. Locate the `ingress_with_source_security_groups` and add the security group id in the array as follows:
```
    /**
        lambda security group = sg-0a4f3903dde4f745e
        since is an array, it is possible to add multiple SG ids, just separate them by ,
   **/
   ingress_with_source_security_groups = ["sg-0a4f3903dde4f745e"]
```

*NOTE: For more information on how to configure the variables in the `terraform.tfvars` file in the kafka governance process project
see the README.md file on that project*

### Environment Variables
It is required to configure the following environment variables:

* `GLUE_REGISTRY_NAME`: Holds the name of the AWS GLue registry to use
* `KAFKA_BOOSTRAP_SERVERS` Kafka boostrap servers
* `REGION`: AWS Region where is being deployed, example: `eu-central-1`

## Expected Input and output

This lambda expects a specific json input in order to be able to publish messages on the MSK also, it always returns the same
json as response, indicating the status of the operation.

### Input
The expected input is a `JSON object` with the following structure

* `body`: (**Required**) `JSON Object` that holds the message to be published on the MSK
* `headers`:(**Optional**) `JSON Object` that holds the headers to add to the message, The JSON Object
can only have pairs of key, values both in stings, no actual object structure is allowed, example:
```
 headers: {
    "my_key":"myvalue",
    "my_other_key:"42" 
 }
```
* `topicName`: (**Required**) `String` with the actual name of the topic
* `key`: :(**Optional**) `String` that holds the key to add to the message

Full Example:

```
{
    "body":{
        "sample_message":"hello world"
    },
    "headers": {
        "my_key":"myvalue",
        "my_other_key:"42" 
    },
    topicName:"MyTopic",
    "key":"messageKey"
}
```

#### Input Validation

This lambda will return an error response if any of the required fields described before are missing also,
the body will be validated against the schema with the same name published in AWS Glue. This validation is required since
the lambda will use that schema to dynamically generate the protobuf message.

### Output
Since this lambda is not an HTTP service, it is not possible to return HTTP codes as a normal
microservice would however, this lambda uses the HTTP standard codes to indicate errors.

The output of this lambda is a JSON Object with the following structure:

* `statusCode`: `String` that indicate the status of the operation, the possible values are:
  * `200`: The message was published.
  * `400`: One expected input attribute is missing or the `body` does not match with the expected protobuf schema.
  * `500`: General error
* `message`: `String` Message that gives more information about the status code, in the case of `statusCode` being `200`,
you will get always `ok`, in any other case, the message will explain a little why the lambda is returning that code. 

Example:

```
{
    "statusCode":"200",
    "message": "ok"
}
```


## Invocation

This lambda is intended to be used in conjunction with KrakenD as a way to
publish messages in kafka though a simple HTTP interface, to know more about how to
configure this integration, please see the [integration_with_krakend.md](integration_with_krakend.md).

It is also possible to use this lambda as a generic way to publish messages in kafka,there is no limitation in
usage as long as the input structure is used and the deployment configuration is done.