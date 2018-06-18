package io.pivotal.jms.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.pivotal.jms.model.JMSProperties;
import io.pivotal.jms.model.Order;
import io.pivotal.jms.model.Shipment;
import io.pivotal.jms.sender.TibcoSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import java.util.Random;
import java.util.UUID;

@RestController
@Slf4j
public class DemoController {

    @Autowired
    TibcoSender tibcoSender;

    @GetMapping(value = "/send")
    public ResponseEntity<Shipment> send() throws JMSException {
        Random random = new Random();
        Order order = new Order(System.currentTimeMillis(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                random.nextInt());

        log.info("--------- Sending {} ", order);
        JMSProperties jmsProperties = JMSProperties.builder()
                .priority(4)
                .deliveryMode(DeliveryMode.NON_PERSISTENT)
                .expiration(30L)
                .requestTimeOut(120L)
                .destinationQueueName("ALS.QA.SPEED.PLATFORM.IN.SERVICEAREAVALIDATION.RECEIVE.Q")
                .correlationId(UUID.randomUUID().toString())
                .replyToQueue(UUID.randomUUID().toString())
                .build();
        String response = tibcoSender.sendTextWithReply(order.toString(), jmsProperties);
        log.info("JMS Response: {}", response);

        //Assuming that we get Shipment json reply
        Gson gson = new GsonBuilder().create();
        Shipment shipment = gson.fromJson(response, Shipment.class);
        return new ResponseEntity<>(shipment, HttpStatus.OK);
    }
}

