package org.psc.json;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DefaultData {

    private String name;

    private Integer discreteValue;

    List<SubData> subData;

    Map<String, Object> embeddedValues;

    List<WrappedData> wrappedData;

}
