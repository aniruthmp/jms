package io.pivotal.jms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@Configuration
public class ActiveMQConfig {

    public static final String ORDER_QUEUE = "order-queue";
    public static final String ORDER_REPLY_2_QUEUE = "order-reply-2-queue";

}