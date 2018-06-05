package io.pivotal.jms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(JmsApplication.class, args);
    }
}

//https://www.codenotfound.com/spring-jms-activemq-example.html
//https://github.com/snicoll/scratches/tree/master/jms-request-reply
//https://stackoverflow.com/questions/43484404/using-spring-jmstemplate-activemq-with-request-response-without-blocking-oth/43489102
//https://memorynotfound.com/spring-jms-setting-reading-header-properties-example/
//http://gpc.github.io/jms/docs/manual/guide/6.%20Receiving%20Messages%20with%20Selectors.html
//https://stackoverflow.com/questions/23836455/difference-between-sessiontransacted-and-jmstransactionmanager
