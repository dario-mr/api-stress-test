package com.dario.ast.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@UtilityClass
public class MapConverter {

    public static MultiValueMap<String, String> convertToMultiValueMap(Map<String, String> map) {
        var multiValueMap = new LinkedMultiValueMap<String, String>();
        map.forEach(multiValueMap::add);

        return multiValueMap;
    }
}
