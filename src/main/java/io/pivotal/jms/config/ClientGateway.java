package io.pivotal.jms.config;

import io.pivotal.jms.model.Order;
import io.pivotal.jms.model.Shipment;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "requests")
public interface ClientGateway {
    Shipment sendAndReceive(Order order);
}

