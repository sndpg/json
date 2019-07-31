package org.psc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class WrappedData {
    @JsonDeserialize(using = ConditionalNullDeserializer.class)
    @NullIf("-999")
    private Integer value;
}
