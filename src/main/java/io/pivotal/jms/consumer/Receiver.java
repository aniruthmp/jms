package io.pivotal.jms.consumer;

import io.pivotal.jms.model.Order;
import io.pivotal.jms.model.Shipment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class Receiver {
    public Shipment receiveMessage(@Payload Order order) {
        Shipment shipment = new Shipment(order.getId(), UUID.randomUUID().toString());
        return shipment;
    }

    public Shipment receiveMessageSleep(@Payload Order order) {
        try {
            Thread.sleep(4500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Shipment shipment = new Shipment(order.getId(), UUID.randomUUID().toString());
        return shipment;
    }
}