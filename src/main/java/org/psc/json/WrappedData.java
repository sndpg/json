package org.psc.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class WrappedData {
//    @NullIf("-999")
//    @JsonDeserialize(using = ConditionalNullDeserializer.class)
    @NullIf("-1")
    private Integer value;
}
