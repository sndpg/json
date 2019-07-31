package org.psc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DefaultData {

    private String name;

    @NullIf("555")
    @JsonDeserialize(using = ConditionalNullDeserializer.class)
    private Integer discreteValue;

    List<SubData> subData;

    Map<String, Object> embeddedValues;

    @JsonDeserialize(contentUsing = ConditionalNullDeserializer.class)
    @NullIf("-999")
    List<WrappedData> wrappedData;

}
