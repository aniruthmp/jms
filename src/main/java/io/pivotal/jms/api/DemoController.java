package io.pivotal.jms.api;

import io.pivotal.jms.config.ClientGateway;
import io.pivotal.jms.model.Order;
import io.pivotal.jms.model.Shipment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.JMSException;
import java.util.Random;
import java.util.UUID;

@RestController
@Slf4j
public class DemoController {

    @Autowired
    private ClientGateway clientGateway;

    @GetMapping(value = "/send")
    public ResponseEntity<Shipment> send() throws JMSException {
        Random random = new Random();
        Order order = new Order(System.currentTimeMillis(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                random.nextInt());
        log.info("--------- Sending {} ", order);
        Shipment shipment = clientGateway.sendAndReceive(order);
        log.info("--------- Received {} ", shipment);

        return new ResponseEntity<>(shipment, HttpStatus.OK);
    }
}

