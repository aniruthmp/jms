package io.pivotal.jms.sender;

import io.pivotal.jms.model.Order;
import io.pivotal.jms.model.Shipment;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.pivotal.jms.config.ActiveMQConfig.ORDER_QUEUE;
import static io.pivotal.jms.config.ActiveMQConfig.ORDER_REPLY_2_QUEUE;

@Slf4j
@Service
public class Producer {

    @Autowired
    JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    JmsTemplate jmsTemplate;

    public Shipment sendWithReply(Order order) throws JMSException {
        jmsTemplate.setReceiveTimeout(1000L);
        jmsMessagingTemplate.setJmsTemplate(jmsTemplate);

        Session session = jmsMessagingTemplate.getConnectionFactory().createConnection()
                .createSession(false, Session.AUTO_ACKNOWLEDGE);

        ObjectMessage objectMessage = session.createObjectMessage(order);

        objectMessage.setJMSCorrelationID(UUID.randomUUID().toString());
        objectMessage.setJMSReplyTo(new ActiveMQQueue(ORDER_REPLY_2_QUEUE));
        objectMessage.setJMSCorrelationID(UUID.randomUUID().toString());
        objectMessage.setJMSExpiration(1000L);
        objectMessage.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);

        return jmsMessagingTemplate.convertSendAndReceive(new ActiveMQQueue(ORDER_QUEUE),
                objectMessage, Shipment.class); //this operation seems to be blocking + sync
    }


}
