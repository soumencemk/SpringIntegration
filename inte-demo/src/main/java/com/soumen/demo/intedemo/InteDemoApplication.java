package com.soumen.demo.intedemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.core.GenericTransformer;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@SpringBootApplication
@IntegrationComponentScan
public class InteDemoApplication {
    static Logger log = LoggerFactory.getLogger(InteDemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(InteDemoApplication.class, args);
    }

    @Bean
    MessageChannel greetings() {
        return MessageChannels.direct().getObject();
    }

    @Bean
    MessageChannel greetingsRequests() {
        return MessageChannels.direct().getObject();
    }


    @Bean
    MessageChannel greetingsResults() {
        return MessageChannels.direct().getObject();
    }

    static String text() {
        return Math.random() > 0.5 ?
                "Hello @" + Instant.now() + " !" :
                "Good Bye!";
    }


    /*@Bean
    ApplicationRunner runner() {
        return args -> {
            for (int i = 0; i < 10; i++) {
                greetings().send(MessageBuilder.withPayload(text()).build());
            }
        };
    }*/

    @Component
    static class MyMessageSource implements MessageSource<String> {

        @Override
        public Message<String> receive() {
            return MessageBuilder.withPayload(text()).build();
        }
    }

    /*@Bean
    ApplicationRunner runner(IntegrationFlowContext context, MyMessageSource messageSource) {
        return args -> {
            var byeFlow = buildFlow(messageSource, 1, "Bye");
            var helloFlow = buildFlow(messageSource, 2, "Hello");
            Set.of(byeFlow, helloFlow).forEach(flow -> context.registration(flow).register().start());

        };
    }*/


    /*private static IntegrationFlow buildFlow(MyMessageSource myMessageSource, int seconds, String filterText) {
        return IntegrationFlow
                .from(myMessageSource, p -> p.poller(pf -> pf.fixedRate(seconds * 1000L)))
                .filter(String.class, source -> source.contains(filterText))
                .transform((GenericTransformer<String, String>) String::toUpperCase)
                .handle((payload, headers) -> {
                    log.info("FilterText : {} || Payload : {}", filterText, payload);
                    return null;
                })
                .get();
    }
*/

    @Bean
    IntegrationFlow integrationFlow(MyMessageSource myMessageSource) {
        return IntegrationFlow
                .from(greetingsRequests())
                //.from(myMessageSource, p -> p.poller(pf -> pf.fixedRate(3000)))
                .filter(String.class, source -> source.contains("Bye"))
                .transform((GenericTransformer<String, String>) String::toUpperCase)
                /*.handle((payload, headers) -> {
                    log.info("The payload is : {}", payload);
                    return null;
                })*/
                .channel(greetingsResults())
                .get();
    }



   /* @Bean
    IntegrationFlow listen() {
        return IntegrationFlow
                .from(atob())
                .handle((payload, headers) -> {
                    System.out.println("The payload is : " + payload);
                    return null;
                })
                .get();
    }*/


}

@Component
class Runner implements ApplicationRunner {
    static Logger log = LoggerFactory.getLogger(Runner.class);
    private final GreetingsClient greetingsClient;

    Runner(GreetingsClient greetingsClient) {
        this.greetingsClient = greetingsClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        /*for (int i = 0; i < 100; i++) {
            String msg = this.greetingsClient.greet(InteDemoApplication.text());
            log.info("Result : {}", msg);
        }*/

        log.info("ENVIRONMENT VARIABLES");
        System.getenv().forEach((k, v) -> log.info("{} ==> {}", k, v));
    }
}


@MessagingGateway
interface GreetingsClient {

    @Gateway(requestChannel = "greetingsRequests", replyChannel = "greetingsResults")
    String greet(String text);
}

