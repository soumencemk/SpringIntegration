package com.soumen.demo.intedemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.GenericSelector;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.dsl.Files;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.SystemPropertyUtils;

import java.io.File;

@Configuration
public class FileExample {

    //https://www.youtube.com/watch?v=vk-hqksT2dI
    @Bean
    MessageChannel filesRequest() {
        return MessageChannels.direct().getObject();
    }

    @Bean
    MessageChannel filesResponse() {
        return MessageChannels.direct().getObject();
    }

    @Bean
    IntegrationFlow inboundFileIntegrationFlow() {
        var directory = new File(SystemPropertyUtils.resolvePlaceholders("C:\\Users\\ezsouka\\WORK\\PERSONAL\\GITHUB\\SpringIntegration\\inte-demo\\src\\main\\resources\\in"));
        return IntegrationFlow
                .from(Files.inboundAdapter(directory).autoCreateDirectory(true))
                .filter((GenericSelector<File>) source -> source.getName().contains("nokia"))
                .channel(filesRequest()).get();
    }

    @Bean
    IntegrationFlow outboundFlow() {
        var directory = new File(SystemPropertyUtils.resolvePlaceholders("C:\\Users\\ezsouka\\WORK\\PERSONAL\\GITHUB\\SpringIntegration\\inte-demo\\src\\main\\resources\\out"));
        return IntegrationFlow
                .from(filesRequest())
                .handle(Files.outboundAdapter(directory).autoCreateDirectory(true))
                .get();
    }
}
