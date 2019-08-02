package org.psc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class NullFilteringCollectionDeserializer extends CollectionDeserializer {
    protected NullFilteringCollectionDeserializer(CollectionDeserializer src) {
        super(src);
    }

    @Override
    public CollectionDeserializer createContextual(DeserializationContext ctxt,
                                                   BeanProperty property) throws JsonMappingException {
        return new NullFilteringCollectionDeserializer(super.createContextual(ctxt, property));
    }

    @Override
    public Collection<Object> deserialize(JsonParser jsonParser,
                                          DeserializationContext deserializationContext) throws IOException {
        Collection<Object> collection = super.deserialize(jsonParser, deserializationContext);

        return collection.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
