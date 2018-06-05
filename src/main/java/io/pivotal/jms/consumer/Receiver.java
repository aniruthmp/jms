package io.pivotal.jms.consumer;

import io.pivotal.jms.model.Order;
import io.pivotal.jms.model.Shipment;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.util.UUID;

import static io.pivotal.jms.config.ActiveMQConfig.ORDER_QUEUE;

@Slf4j
@Component
public class Receiver implements SessionAwareMessageListener<Message> {

    @Override
    @JmsListener(destination = ORDER_QUEUE)
    public void onMessage(Message message, Session session) throws JMSException {
        log.info("message.getJMSCorrelationID(): {}", message.getJMSCorrelationID());
        log.info("message.getJMSExpiration(): {}", message.getJMSExpiration());
        log.info("message.getJMSDeliveryMode(): {}", message.getJMSDeliveryMode());
        log.info("message.getJMSReplyTo(): {}", message.getJMSReplyTo().toString());
        log.info("message.getJMSDestination(): {}", message.getJMSDestination().toString());

        Order order = (Order) ((ActiveMQObjectMessage) message).getObject();
        Shipment shipment = new Shipment(order.getId(), UUID.randomUUID().toString());

        // done handling the request, now create a response message
        final ObjectMessage responseMessage = new ActiveMQObjectMessage();
        responseMessage.setJMSCorrelationID(message.getJMSCorrelationID());
        responseMessage.setObject(shipment);

        // Message sent back to the replyTo address of the income message.
        log.info("JMSReplyTo: {}", message.getJMSReplyTo());
        final MessageProducer producer = session.createProducer(message.getJMSReplyTo());
        producer.send(responseMessage);
    }
}