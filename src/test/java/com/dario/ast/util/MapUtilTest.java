package com.dario.ast.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.dario.ast.util.MapUtil.removeEmptyEntries;
import static org.assertj.core.api.Assertions.assertThat;

public class MapUtilTest {

    @Test
    void removeEmptyEntries_whenMapIsEmpty_shouldReturnEmptyMap() {
        // given
        var emptyMap = new HashMap<String, String>();

        // when
        var cleanedMap = removeEmptyEntries(emptyMap);

        // then
        assertThat(cleanedMap).isEmpty();
    }

    @Test
    void removeEmptyEntries_whenMapHasNoEmptyEntries_shouldDoNothing() {
        // given
        var map = Map.of(
                "key1", "value1",
                "key2", "value2");

        // when
        var cleanedMap = removeEmptyEntries(map);

        // then
        assertThat(cleanedMap).isEqualTo(map);
    }

    @Test
    void removeEmptyEntries_whenMapHasEmptyEntries_shouldRemoveEmptyEntries() {
        // given
        var map = Map.of(
                "key1", "value1",
                "key2", "value2",
                "", "");

        // when
        var cleanedMap = removeEmptyEntries(map);

        // then
        assertThat(cleanedMap).isEqualTo(Map.of(
                "key1", "value1",
                "key2", "value2"));
    }

    @Test
    void removeEmptyEntries_whenMapIsNull_shouldReturnNull() {
        // when
        var cleanedMap = removeEmptyEntries(null);

        // then
        assertThat(cleanedMap).isNull();
    }
}
