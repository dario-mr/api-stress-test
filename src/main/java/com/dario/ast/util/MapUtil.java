package com.dario.ast.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static java.util.Map.Entry;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.StringUtils.hasText;

@UtilityClass
public class MapUtil {

    public static Map<String, String> removeEmptyEntries(Map<String, String> map) {
        if (map == null) {
            return null;
        }

        return map.entrySet().stream()
                .filter(entry -> hasText(entry.getKey()) && hasText(entry.getValue()))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    public static MultiValueMap<String, String> convertToMultiValueMap(Map<String, String> map) {
        var multiValueMap = new LinkedMultiValueMap<String, String>();
        map.forEach(multiValueMap::add);

        return multiValueMap;
    }
}
