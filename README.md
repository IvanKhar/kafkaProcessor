# kafkaProcessor
Kafka stream processor. This application is monitoring a certain directory on CREATE events, on each event it puts a message to Kafka topic. Another module using Kafka stream api, monitors this topic, aggregates this messages and once in a certain time calls another REST service
