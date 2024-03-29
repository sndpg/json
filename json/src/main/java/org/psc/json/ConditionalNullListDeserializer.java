package org.psc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.node.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Iterator;

@Slf4j
@NoArgsConstructor
public class ConditionalNullListDeserializer extends JsonDeserializer implements ContextualDeserializer {

    private BeanProperty property;

    private ConditionalNullListDeserializer(BeanProperty property) {
        this.property = property;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
                                                BeanProperty property) throws JsonMappingException {
        return new ConditionalNullListDeserializer(property);
    }

    @Override
    public Object deserialize(JsonParser jsonParser,
                              DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
//        log.info(property.getName());
        Class<?> clazz = property.getType().getRawClass();
        NullIf nullIf = property.getAnnotation(NullIf.class);

        Object result = null;
        if (nullIf != null) {
            String value = nullIf.value();

            JavaType type = deserializationContext.getContextualType();
            this.property.getType();
            JsonDeserializer valueDeserializer = deserializationContext.findNonContextualValueDeserializer(this.property.getType());

            ArrayNode node = jsonParser.getCodec().readTree(jsonParser);

            node.remove(0);
            if (node.getNodeType() == JsonNodeType.OBJECT) {
                Iterator<JsonNode> nodeIterator = node.elements();
                while(nodeIterator.hasNext()){
                    result = getNodeValue(nodeIterator.next(), value);
                    if (result == null){
//                        log.info("I should be removed... please, I beg you");
                    }
                }
            } else {
                result = getNodeValue(node, value);
            }


        }

        if (result == null){
            jsonParser.clearCurrentToken();
        }

        return null;
    }


    private Object getNodeValue(JsonNode node, String value){
        Object result = null;

        switch (node.getNodeType()) {
            case ARRAY:
                result = new Object[0];
                break;
            case BINARY:
                break;
            case BOOLEAN:
                boolean filterCriteria = Boolean.parseBoolean(value);
                result = filterCriteria != node.asBoolean() ? node.asBoolean() : null;
                break;
            case MISSING:
                break;
            case NULL:
                break;
            case NUMBER:
                if (node instanceof IntNode) {
                    int intFilerCriteria = Integer.parseInt(value);
                    result = intFilerCriteria != node.asInt() ? node.asInt() : null;
                } else if (node instanceof DoubleNode) {
                    double doubleFilterCriteria = Double.parseDouble(value);
                    result = doubleFilterCriteria != node.asDouble() ? node.asDouble() : null;
                }
                break;
            case OBJECT:
                ObjectNode objectNode = (ObjectNode) node;
                Iterator it = objectNode.elements();
                objectNode.getNodeType();
                break;
            case POJO:
                break;
            case STRING:
                result = !value.equals(node.asText()) ? node.asText() : null;
                break;
        }
        return result;
    }

}
