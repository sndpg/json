package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.std.AsArraySerializerBase;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is an optimized serializer for Lists that can be efficiently
 * traversed by index (as opposed to others, such as {@link LinkedList}
 * that cannot}.
 */
@JacksonStdImpl
public final class IndexedListSerializer2 extends AsArraySerializerBase<List<?>> {
    private static final long serialVersionUID = 1L;

    private static final Set<Class<?>> BASE_TYPES =
            Set.of(Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class,
                    Double.class, Void.class, String.class);

    public IndexedListSerializer2(JavaType elemType, boolean staticTyping, TypeSerializer vts,
                                  JsonSerializer<Object> valueSerializer) {
        super(List.class, elemType, staticTyping, vts, valueSerializer);
    }

    public IndexedListSerializer2(IndexedListSerializer2 src, BeanProperty property, TypeSerializer vts,
                                  JsonSerializer<?> valueSerializer, Boolean unwrapSingle) {
        super(src, property, vts, valueSerializer, unwrapSingle);
    }

    @Override
    public IndexedListSerializer2 withResolved(BeanProperty property, TypeSerializer vts,
                                               JsonSerializer<?> elementSerializer, Boolean unwrapSingle) {
        return new IndexedListSerializer2(this, property, vts, elementSerializer, unwrapSingle);
    }

    /*
    /**********************************************************
    /* Accessors
    /**********************************************************
     */

    @Override
    public boolean isEmpty(SerializerProvider prov, List<?> value) {
        return value.isEmpty();
    }

    @Override
    public boolean hasSingleElement(List<?> value) {
        return (value.size() == 1);
    }

    @Override
    public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
        return new IndexedListSerializer2(this, _property, vts, _elementSerializer, _unwrapSingle);
    }

    @Override
    public final void serialize(List<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        value = value.stream().filter(e -> {
            try {
                return !checkAllFieldsNull(((Object) e).getClass(), e);
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
            return true;
        }).collect(Collectors.toList());

        final int len = value.size();

        if (len == 1) {
            if (((_unwrapSingle == null) &&
                    provider.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)) ||
                    (_unwrapSingle == Boolean.TRUE)) {
                serializeContents(value, gen, provider);
                return;
            }
        }
        gen.writeStartArray(len);
        serializeContents(value, gen, provider);
        gen.writeEndArray();
    }

    @Override
    public void serializeContents(List<?> value, JsonGenerator g, SerializerProvider provider) throws IOException {
        if (_elementSerializer != null) {
            serializeContentsUsing(value, g, provider, _elementSerializer);
            return;
        }
        if (_valueTypeSerializer != null) {
            serializeTypedContents(value, g, provider);
            return;
        }
        final int len = value.size();
        if (len == 0) {
            return;
        }
        int i = 0;
        try {
            PropertySerializerMap serializers = _dynamicSerializers;
            for (; i < len; ++i) {
                Object elem = value.get(i);
                if (elem == null) {
                    provider.defaultSerializeNull(g);
                } else {
                    Class<?> cc = elem.getClass();
                    JsonSerializer<Object> serializer = serializers.serializerFor(cc);
                    if (serializer == null) {
                        // To fix [JACKSON-508]
                        if (_elementType.hasGenericTypes()) {
                            serializer =
                                    _findAndAddDynamic(serializers, provider.constructSpecializedType(_elementType, cc),
                                            provider);
                        } else {
                            serializer = _findAndAddDynamic(serializers, cc, provider);
                        }
                        serializers = _dynamicSerializers;
                    }
                    serializer.serialize(elem, g, provider);
                }
            }
        } catch (Exception e) {
            wrapAndThrow(provider, e, value, i);
        }
    }

    public void serializeContentsUsing(List<?> value, JsonGenerator jgen, SerializerProvider provider,
                                       JsonSerializer<Object> ser) throws IOException {
        final int len = value.size();
        if (len == 0) {
            return;
        }
        final TypeSerializer typeSer = _valueTypeSerializer;
        for (int i = 0; i < len; ++i) {
            Object elem = value.get(i);
            try {
                if (elem == null) {
                    provider.defaultSerializeNull(jgen);
                } else if (typeSer == null) {
                    ser.serialize(elem, jgen, provider);
                } else {
                    ser.serializeWithType(elem, jgen, provider, typeSer);
                }
            } catch (Exception e) {
                // [JACKSON-55] Need to add reference information
                wrapAndThrow(provider, e, value, i);
            }
        }
    }

    public void serializeTypedContents(List<?> value, JsonGenerator jgen,
                                       SerializerProvider provider) throws IOException {
        final int len = value.size();
        if (len == 0) {
            return;
        }
        int i = 0;
        try {
            final TypeSerializer typeSer = _valueTypeSerializer;
            PropertySerializerMap serializers = _dynamicSerializers;
            for (; i < len; ++i) {
                Object elem = value.get(i);
                if (elem == null) {
                    provider.defaultSerializeNull(jgen);
                } else {
                    Class<?> cc = elem.getClass();
                    JsonSerializer<Object> serializer = serializers.serializerFor(cc);
                    if (serializer == null) {
                        // To fix [JACKSON-508]
                        if (_elementType.hasGenericTypes()) {
                            serializer =
                                    _findAndAddDynamic(serializers, provider.constructSpecializedType(_elementType, cc),
                                            provider);
                        } else {
                            serializer = _findAndAddDynamic(serializers, cc, provider);
                        }
                        serializers = _dynamicSerializers;
                    }
                    serializer.serializeWithType(elem, jgen, provider, typeSer);
                }
            }
        } catch (Exception e) {
            // [JACKSON-55] Need to add reference information
            wrapAndThrow(provider, e, value, i);
        }
    }

    private boolean checkAllFieldsNull(Class<?> clazz, Object object) throws IllegalAccessException {
        if (object == null) {
            return true;
        }
        if (BASE_TYPES.contains(clazz)) {
            return false;
        }

        Field[] fields = clazz.getDeclaredFields();
        boolean allFieldsNull = fields.length > 0;

        for (Field field : fields) {
            field.setAccessible(true);
            Object currentObject = field.get(object);
            if (currentObject != null) {
                allFieldsNull &= checkAllFieldsNull(field.getType(), currentObject);
            }
            field.setAccessible(false);
        }

        return allFieldsNull;
    }
}
