package org.psc.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.std.AsArraySerializerBase;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class ListFilterSerializer extends JsonSerializer {

    private ObjectMapper objectMapper;
    private JsonSerializer serializer;

    public ListFilterSerializer(ObjectMapper objectMapper, JsonSerializer serializer) {
        this.objectMapper = objectMapper;
        this.serializer = serializer;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        List<?> values = new ArrayList<>();
        JsonNode node = objectMapper.valueToTree(value);
        if (node != null) {
            Iterator<JsonNode> nodes = node.elements();
            while (nodes.hasNext()) {
                JsonNode currentNode = nodes.next();
                if (currentNode.isEmpty(serializers)) {
                    nodes.remove();
                }
            }
        }
        if (value instanceof List) {
            values = ((List<?>) value).stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
        log.info(value.toString());
        serializer.serialize(values, gen, serializers);

    }


}
