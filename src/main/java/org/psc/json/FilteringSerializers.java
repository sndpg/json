package org.psc.json;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.impl.IndexedListSerializer;
import com.fasterxml.jackson.databind.ser.impl.IndexedStringListSerializer;
import com.fasterxml.jackson.databind.ser.impl.StringCollectionSerializer;
import com.fasterxml.jackson.databind.ser.std.CollectionSerializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.RandomAccess;

@Slf4j
public class FilteringSerializers extends SimpleSerializers {

    private boolean staticTyping = false;

    private ObjectMapper objectMapper;
    private ArrayFilterSerializer arrayFilterSerializer = new ArrayFilterSerializer();
    private ListFilterSerializer listFilterSerializer;

    public FilteringSerializers(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonSerializer<?> findArraySerializer(SerializationConfig config, ArrayType type, BeanDescription beanDesc,
                                                 TypeSerializer elementTypeSerializer,
                                                 JsonSerializer<Object> elementValueSerializer) {
        return arrayFilterSerializer;
    }

    @Override
    public JsonSerializer<?> findCollectionSerializer(SerializationConfig config, CollectionType type,
                                                      BeanDescription beanDesc, TypeSerializer elementTypeSerializer,
                                                      JsonSerializer<Object> elementValueSerializer) {
        return findCollectionLikeSerializer(config, type, beanDesc, elementTypeSerializer, elementValueSerializer);
    }

    @Override
    public JsonSerializer<?> findCollectionLikeSerializer(SerializationConfig config, CollectionLikeType type,
                                                          BeanDescription beanDesc,
                                                          TypeSerializer elementTypeSerializer,
                                                          JsonSerializer<Object> elementValueSerializer) {
        log.info(beanDesc.getType().toString());
        if (listFilterSerializer == null) {
            listFilterSerializer =
                    new ListFilterSerializer(objectMapper, createSerializer(type, elementTypeSerializer, elementValueSerializer));
        }
        return listFilterSerializer;
    }


    private JsonSerializer createSerializer(JavaType type, TypeSerializer elementTypeSerializer,
                                            JsonSerializer<Object> elementValueSerializer) {
        JsonSerializer ser = null;
        Class<?> raw = type.getRawClass();
        Class<?> elementRaw = type.getContentType().getRawClass();
        if (isIndexedList(raw)) {
            if (elementRaw == String.class) {
                // [JACKSON-829] Must NOT use if we have custom serializer
                if (ClassUtil.isJacksonStdImpl(elementValueSerializer)) {
                    ser = IndexedStringListSerializer.instance;
                }
            } else {
                ser = buildIndexedListSerializer(type.getContentType(), staticTyping, elementTypeSerializer,
                        elementValueSerializer);
            }
        } else if (elementRaw == String.class) {
            // [JACKSON-829] Must NOT use if we have custom serializer
            if (ClassUtil.isJacksonStdImpl(elementValueSerializer)) {
                ser = StringCollectionSerializer.instance;
            }
        }
        if (ser == null) {
            ser = buildCollectionSerializer(type.getContentType(), staticTyping, elementTypeSerializer,
                    elementValueSerializer);
        }
        return ser;
    }

    protected boolean isIndexedList(Class<?> cls) {
        return RandomAccess.class.isAssignableFrom(cls);
    }

    public ContainerSerializer<?> buildIndexedListSerializer(JavaType elemType, boolean staticTyping,
                                                             TypeSerializer vts,
                                                             JsonSerializer<Object> valueSerializer) {
        return new IndexedListSerializer(elemType, staticTyping, vts, valueSerializer);
    }

    public ContainerSerializer<?> buildCollectionSerializer(JavaType elemType, boolean staticTyping, TypeSerializer vts,
                                                            JsonSerializer<Object> valueSerializer) {
        return new CollectionSerializer(elemType, staticTyping, vts, valueSerializer);
    }


}
