package org.psc.json;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class DefaultData {

    private String name;

    @NullIf("555")
    @JsonDeserialize(using = ConditionalNullDeserializer.class)
    private Integer discreteValue;

    private List<SubData> subData;

    private Map<String, Object> embeddedValues;

    private List<WrappedData> wrappedData;

//    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
//    public void setWrappedData(List<WrappedData> wrappedData) {
//        this.wrappedData = wrappedData.stream().filter(Objects::nonNull).collect(Collectors.toList());
//    }

}
