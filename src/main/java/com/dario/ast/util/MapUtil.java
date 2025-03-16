package com.dario.ast.util;

import static java.util.Map.Entry;
import static java.util.stream.Collectors.toMap;

import com.dario.ast.core.domain.EnvVariable;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@UtilityClass
public class MapUtil {

  public static Map<String, String> removeEmptyEntries(Map<String, String> map) {
    if (map == null) {
      return null;
    }

    return map.entrySet().stream()
        .filter(entry -> !entry.getKey().isBlank() && !entry.getValue().isBlank())
        .collect(toMap(Entry::getKey, Entry::getValue));
  }

  public static Map<String, EnvVariable> removeEnvVarEmptyEntries(Map<String, EnvVariable> map) {
    if (map == null) {
      return null;
    }

    return map.entrySet().stream()
        .filter(entry -> !entry.getKey().isBlank())
        .collect(toMap(
            Entry::getKey, Entry::getValue,
            (e1, e2) -> e1,
            LinkedHashMap::new
        ));
  }

  public static MultiValueMap<String, String> convertToMultiValueMap(Map<String, String> map) {
    var multiValueMap = new LinkedMultiValueMap<String, String>();
    map.forEach(multiValueMap::add);

    return multiValueMap;
  }
}
