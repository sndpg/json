package org.psc.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import io.vavr.control.Try;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NullFilteringCollectionDeserializer extends CollectionDeserializer {

    private static final Set<Class<?>> BASE_TYPES =
            Set.of(Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class,
                    Double.class, Void.class, String.class);

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

        return collection.stream()
                .filter(e -> !NullFilteringCollectionDeserializer.checkAllFieldsNull(e))
                .collect(Collectors.toList());
    }

    private static boolean checkAllFieldsNull(Object object) {
        if (object == null) {
            return true;
        }
        Class<?> clazz = object.getClass();
        if (BASE_TYPES.contains(clazz)) {
            return false;
        }

        Field[] fields = clazz.getDeclaredFields();
        boolean allFieldsNull = fields.length > 0;

        for (Field field : fields) {
            field.setAccessible(true);
            Object currentObject = Try.of(() -> field.get(object))
                    .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);
            if (currentObject != null) {
                allFieldsNull &= checkAllFieldsNull(currentObject);
            }
            field.setAccessible(false);
        }

        return allFieldsNull;
    }
}
