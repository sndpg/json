package org.psc.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Slf4j
@SpringBootApplication
public class JsonApplication {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(JsonApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomething() throws IOException {
        var data = new ClassPathResource("data.json");
        var defaultData = objectMapper.readValue(data.getInputStream(), DefaultData.class);

        log.info(objectMapper.writeValueAsString(defaultData));

        SpringApplication.exit(applicationContext);
    }


    @Configuration
    static class JsonApplicationConfiguration {

        @Autowired
        public void configure(ObjectMapper objectMapper) {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            log.info("hi");
        }

    }
}
