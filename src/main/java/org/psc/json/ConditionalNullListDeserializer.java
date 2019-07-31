package org.psc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

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
        log.info(property.getName());
        Class<?> clazz = property.getType().getRawClass();
        NullIf nullIf = property.getAnnotation(NullIf.class);

        TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);

        TreeNode arrayNode = jsonParser.getCodec().createArrayNode();
        arrayNode.traverse().nextValue();

        return null;
    }

}
