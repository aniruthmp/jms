package io.pivotal.jms.config;

import com.tibco.tibjms.TibjmsConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import java.util.Properties;


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
    public TibjmsConnectionFactory tibjmsConnectionFactory() {
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