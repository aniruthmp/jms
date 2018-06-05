//package io.pivotal.jms;
//
//import io.pivotal.jms.consumer.Receiver;
//import io.pivotal.jms.sender.Sender;
//import org.apache.activemq.junit.EmbeddedActiveMQBroker;
//import org.junit.ClassRule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.concurrent.TimeUnit;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@DirtiesContext
//public class JmsApplicationTests {
//
//    @ClassRule
//    public static EmbeddedActiveMQBroker broker = new EmbeddedActiveMQBroker();
//
//    @Autowired
//    private Sender sender;
//
//    @Autowired
//    private Receiver receiver;
//
//    @Test
//    public void testReceive() throws Exception {
//        sender.send("helloworld.q", "Hello Spring JMS ActiveMQ!");
//
//        receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
//        assertThat(receiver.getLatch().getCount()).isEqualTo(0);
//    }
//}
