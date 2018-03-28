package com.github.vkorobkov.jfixtures.domain;

import com.github.vkorobkov.jfixtures.util.CollectionUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;

import java.util.Collections;
import java.util.Map;

@EqualsAndHashCode
@Getter
public final class Row {
    private final String name;
    private final Map<String, Value> columns;

    public Row(String name, Map<String, ?> columns) {
        this.name = name;
        this.columns = Collections.unmodifiableMap(
            CollectionUtil.mapValues(columns, Value::of)
        );
    }

    public Row withBaseColumns(Map<String, Object> base) {
        if (base.isEmpty()) {
            return this;
        }

        Map<String, Object> mappedColumns = CollectionUtil.mapValues(columns, Value::of);
        val mergedColumns = CollectionUtil.merge(base, mappedColumns);
        return new Row(name, mergedColumns);
    }
}
