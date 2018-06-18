package io.pivotal.jms.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.TextMessage;
import java.util.Objects;

@Slf4j
@Component
public class Receiver {

    @JmsListener(destination = "DEMO.REQUEST.QUEUE", containerFactory = "jmsListenerContainerFactory")
    public void receiveMessageFromResponseQueue(TextMessage message) {
        log.info("Received : " + Objects.toString(message, ""));
    }
}