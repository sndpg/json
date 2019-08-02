package org.psc.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class ConditionalNullConverter implements Converter {

    @Override
    public Object convert(Object value) {
        List<Object> valueAsList = (List<Object>) value;
        return filterCollection(valueAsList);
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructCollectionType(List.class, Object.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructCollectionType(List.class, Object.class);
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
}
