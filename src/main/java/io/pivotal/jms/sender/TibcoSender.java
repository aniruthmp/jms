package io.pivotal.jms.sender;

import com.tibco.tibjms.TibjmsQueue;
import io.pivotal.jms.model.JMSProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

@Slf4j
@Service
public class TibcoSender {

    @Autowired
    JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    JmsTemplate jmsTemplate;

    public String sendTextWithReply(String message, JMSProperties jmsProperties) throws JMSException {
        jmsTemplate.setReceiveTimeout(jmsProperties.getRequestTimeOut());
        jmsMessagingTemplate.setJmsTemplate(jmsTemplate);

        return jmsMessagingTemplate.convertSendAndReceive(new TibjmsQueue(jmsProperties.getDestinationQueueName()),
                getTextMessage(message, jmsProperties), String.class);

    }

    public <T> T sendObjectWithObjectReply(String message, JMSProperties jmsProperties,
                                           Class<T> targetClass) throws JMSException {
        jmsTemplate.setReceiveTimeout(jmsProperties.getRequestTimeOut());
        jmsMessagingTemplate.setJmsTemplate(jmsTemplate);

        return jmsMessagingTemplate.convertSendAndReceive(new TibjmsQueue(jmsProperties.getDestinationQueueName()),
                getTextMessage(message, jmsProperties), targetClass);

    }

    private TextMessage getTextMessage(String message, JMSProperties jmsProperties) throws JMSException {
        Session session = jmsMessagingTemplate.getConnectionFactory().createConnection()
                .createSession(false, Session.AUTO_ACKNOWLEDGE);

        TextMessage textMessage = session.createTextMessage(message);
        textMessage.setJMSCorrelationID(jmsProperties.getCorrelationId());
        textMessage.setJMSReplyTo(new TibjmsQueue(jmsProperties.getReplyToQueue()));
        textMessage.setJMSExpiration(jmsProperties.getExpiration());
        textMessage.setJMSDeliveryMode(jmsProperties.getDeliveryMode());
        textMessage.setJMSPriority(jmsProperties.getPriority());

        log.info("getTextMessage JMS Properties: {}", jmsProperties.toString());
        log.info("getTextMessage returns: {}", textMessage.getText());

        return textMessage;
    }
}