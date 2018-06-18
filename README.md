# Spring JMS with Tibco

### Pre-requisite:
Since Tibco EMS is a licensed software, ensure that you have proper connectivity to your Tibco EMS server(s). Accordingly change the properties inside [application.yml](./src/main/resources/application.yml)

1. Add the dependencies for jms and for tibco
    ```xml
    <!-- tibco -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-jms</artifactId>
    </dependency>
    <dependency>
        <groupId>com.tibco</groupId>
        <artifactId>tibjms</artifactId>
        <version>4.3.0</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/lib/tibjms.jar</systemPath>
    </dependency>
    <dependency>
        <groupId>javax.jms</groupId>
        <artifactId>jms</artifactId>
    </dependency>
    ```

1. Define all your Tibco configuration as below.
    ```java
    @Configuration
    @Slf4j
    @EnableJms
    public class TibcoConfig {

        @Value("${tibco.jndiName}")
        String jndiName;

        @Value("${tibco.server.url}")
        String serverUrl;

        @Value("${tibco.server.principal}")
        String principal;

        @Value("${tibco.server.password}")
        String password;

        @Bean
        public JndiTemplate jndiTemplate() {
            Properties environment = new Properties();
            environment.put("java.naming.factory.initial", "com.tibco.tibjms.naming.TibjmsInitialContextFactory");
            environment.put("java.naming.provider.url", serverUrl);
            environment.put("java.naming.security.principal", principal);
            environment.put("java.naming.security.credentials", password);
            JndiTemplate jndiTemplate = new JndiTemplate(environment);
            return jndiTemplate;
        }

        @Bean
        @Primary
        public JndiObjectFactoryBean jmsConnectionFactory() {
            JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
            jndiObjectFactoryBean.setJndiTemplate(jndiTemplate());
            jndiObjectFactoryBean.setJndiName(jndiName);
            return jndiObjectFactoryBean;
        }

        @Bean
        public TibjmsConnectionFactory tibjmsConnectionFactory()  {
            TibjmsConnectionFactory tibjmsConnectionFactory = (TibjmsConnectionFactory) jmsConnectionFactory().getObject();
            tibjmsConnectionFactory.setUserName(principal);
            tibjmsConnectionFactory.setUserPassword(password);
            return tibjmsConnectionFactory;
        }

        @Bean
        public JmsTemplate jmsTemplate() {
            JmsTemplate jmsTemplate = new JmsTemplate();
            jmsTemplate.setConnectionFactory(tibjmsConnectionFactory());
            jmsTemplate.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
    //        jmsTemplate.setDefaultDestinationName(tibcoRequestQueue); //This is optional
            jmsTemplate.setSessionTransacted(false);
            return jmsTemplate;
        }

        @Bean
        public JmsMessagingTemplate jmsMessagingTemplate() {
            JmsMessagingTemplate jmsMessagingTemplate = new JmsMessagingTemplate();
            jmsMessagingTemplate.setJmsTemplate(jmsTemplate());
            jmsMessagingTemplate.setConnectionFactory(tibjmsConnectionFactory());
            return jmsMessagingTemplate;
        }

        @Bean
        public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
                DefaultJmsListenerContainerFactoryConfigurer configurer) {
            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
            factory.setConnectionFactory((ConnectionFactory) jmsConnectionFactory().getObject());

            // anonymous class
            factory.setErrorHandler(
                    t -> log.error("An error has occurred in the transaction"));

            factory.setErrorHandler(t -> log.error("An error has occurred in the transaction"));
            configurer.configure(factory, tibjmsConnectionFactory());
            return factory;
        }

        @Bean
        public SimpleMessageConverter simpleMessageConverter() {
            return new SimpleMessageConverter();
        }

    }
    ```
1. Couple of things to note here. In our scenario both these credentials were the same
    - Ensure to set the right JNDI credentials when defining the bean _**JndiTemplate**_
    - Ensure to set the right JMS credentials when defining the bean _**TibjmsConnectionFactory**_
1. Add a JMS listener for receiving messages from the Queue
    ```java
    import org.springframework.jms.annotation.JmsListener;
    import org.springframework.stereotype.Component;

    import javax.jms.TextMessage;
    import java.util.Objects;

    @Component
    public class TibcoListener {
        
        @JmsListener(destination = "DEMO.REQUEST.QUEUE", containerFactory = "jmsListenerContainerFactory")
        public void receiveMessageFromResponseQueue(TextMessage message) {
            log.info("Received : " + Objects.toString(message, ""));
            // Some business logic here 
        }
    }
    ```

1. Now create the _Component_ for sending message. In the snippet below, it performs synchronous REQUEST/REPLY with a dynamic Reply2Queue (ensure to set the _**receiveTimeout**_ to an acceptable value).

    ```java
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

    ```

1. Move configuration to applicaion.yml

    ```yml
    tibco:
        server:
            url: "tibjmsnaming://tibjndi.example.com:7263"
            principal: <USERNAME>
            password: <PASSWORD>
    jndiName: DEMO.TEST.QUEUE
    ```

### Note: 

In case you want the application to connect to Tibco without credentials and JNDI, set the property as `tibco.server.url: "tcp://tibco.example.com:5555"`. 
