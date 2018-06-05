package io.pivotal.jms.config;

import io.pivotal.jms.consumer.Receiver;
import io.pivotal.jms.model.Order;
import io.pivotal.jms.model.Shipment;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.jms.JmsOutboundGateway;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.messaging.MessageChannel;

@EnableIntegration
@IntegrationComponentScan
//@EnableJms
@Configuration
public class ActiveMQConfig {

    public static final String ORDER_QUEUE = "order-queue";
    public static final String ORDER_REPLY_2_QUEUE = "order-reply-2-queue";

    @Bean
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Bean
    public MessageChannel requests() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "requests")
    public JmsOutboundGateway jmsGateway(ActiveMQConnectionFactory activeMQConnectionFactory) {
        JmsOutboundGateway gateway = new JmsOutboundGateway();
        gateway.setConnectionFactory(activeMQConnectionFactory);
        gateway.setRequestDestinationName(ORDER_QUEUE);
        gateway.setReplyDestinationName(ORDER_REPLY_2_QUEUE);
        gateway.setCorrelationKey("JMSCorrelationID");
        gateway.setSendTimeout(100L);
        gateway.setReceiveTimeout(100L);
        return gateway;
    }

    @Autowired
    Receiver receiver;

    @Bean
    public DefaultMessageListenerContainer responder(ActiveMQConnectionFactory activeMQConnectionFactory) {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(activeMQConnectionFactory);
        container.setDestinationName(ORDER_QUEUE);
        MessageListenerAdapter adapter = new MessageListenerAdapter(new Object() {

            @SuppressWarnings("unused")
            public Shipment handleMessage(Order order) {
                return receiver.receiveMessage(order);
            }

        });
        container.setMessageListener(adapter);
        return container;
    }
}