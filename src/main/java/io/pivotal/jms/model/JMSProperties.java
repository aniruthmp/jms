package io.pivotal.jms.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JMSProperties {
    private int priority;
    private int deliveryMode;
    private long expiration;
    private long requestTimeOut;
    private String destinationQueueName;
    private String correlationId;
    private String replyToQueue;
}
