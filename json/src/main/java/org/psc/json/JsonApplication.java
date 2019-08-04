package org.psc.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
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
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

//        Map<String, Object> deserializedMap =
//                objectMapper.readValue(data.getInputStream(), new TypeReference<Map<String, Object>>() {});
//        var filteredMap = filterMap(deserializedMap);
//
//        log.info(objectMapper.writeValueAsString(filteredMap));

        var defaultData = objectMapper.readValue(data.getInputStream(), DefaultData.class);

        var result = objectMapper.writeValueAsString(defaultData);

//        log.info(result);

        SpringApplication.exit(applicationContext);
    }

    public static Map<String, Object> filterMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        // we just assume <String, Object>, since all jsons consist of such pairs
        Map<String, Object> filteredMap = map.entrySet().stream().map(entry -> {
            if (entry == null || entry.getValue() == null) {
                return null;
            }

            if (Map.class.isAssignableFrom(entry.getValue().getClass())) {
                Map<String, Object> value = filterMap((Map<String, Object>) entry.getValue());
                if (value == null || value.isEmpty()) {
                    entry = null;
                } else {
                    entry.setValue(value);
                }
            } else if (Collection.class.isAssignableFrom(entry.getValue().getClass())) {
                Collection<Object> value = filterCollection((Collection<Object>) entry.getValue());
                if (value == null || value.isEmpty()) {
                    entry = null;
                } else {
                    entry.setValue(value);
                }
            }
            return entry;
        }).filter(Objects::nonNull).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (filteredMap.isEmpty()) {
            filteredMap = null;
        }

        return filteredMap;
    }

    public static Collection<Object> filterCollection(Collection<Object> elements) {
        if (elements == null) {
            return null;
        }

        Collection<Object> filteredCollection = elements.stream().map(element -> {
            if (element == null) {
                return null;
            }

            if (Map.class.isAssignableFrom(element.getClass())) {
                Map<String, Object> value = filterMap((Map<String, Object>) element);
                if (value == null || value.isEmpty()) {
                    element = null;
                } else {
                    element = value;
                }
            } else if (Collection.class.isAssignableFrom(element.getClass())) {
                Collection<Object> value = filterCollection((Collection<Object>) element);
                if (value == null || value.isEmpty()) {
                    element = null;
                } else {
                    element = value;
                }
            }
            return element;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        if (filteredCollection.isEmpty()) {
            filteredCollection = null;
        }

        return filteredCollection;

    }

    @Configuration
    static class JsonApplicationConfiguration {

        @Autowired
        public void configure(ObjectMapper objectMapper) {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            var filteringSerializersModule = new SimpleModule();
            filteringSerializersModule.setSerializers(new FilteringSerializers(new ObjectMapper()));

            //   objectMapper.registerModule(filteringSerializersModule);
            objectMapper.registerModule(new CustomCollectionDeserializerModule());
//            log.info("hi");
        }

    }

    static class CustomCollectionDeserializerModule extends SimpleModule {
        @Override
        public void setupModule(SetupContext context) {
            super.setupModule(context);
            context.addBeanDeserializerModifier(new BeanDeserializerModifier() {
                @Override
                public JsonDeserializer<?> modifyCollectionDeserializer(DeserializationConfig config,
                                                                        CollectionType type, BeanDescription beanDesc,
                                                                        JsonDeserializer<?> deserializer) {
                    if (deserializer instanceof CollectionDeserializer) {
                        return new NullFilteringCollectionDeserializer((CollectionDeserializer) deserializer);
                    } else {
                        return super.modifyCollectionDeserializer(config, type, beanDesc, deserializer);
                    }
                }
            });

        }
    }
}
