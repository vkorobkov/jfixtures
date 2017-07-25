package com.github.vkorobkov.jfixtures.config;

import java.util.Collections;
import java.util.stream.Stream;

public class TablesConfig extends Config {
    private static final String SECTION_TABLES = "tables";
    private static final String SECTION_APPLIES_TO = "applies_to";
    private static final String SECTION_PRIMARY_KEY = "pk";
    private static final String SECTION_GENERATE = "generate";
    private static final String SECTION_COLUMN = "column";
    private static final String PK_DEFAULT_COLUMN_NAME = "id";

    public TablesConfig(final YamlConfig yamlConfig) {
        super(yamlConfig);
    }

    public boolean shouldAutoGeneratePk(String tableName) {
        return getMatchingTables(tableName)
                .map(this::extractGenerateValue)
                .reduce((current, last) -> last).orElse(true);
    }

    public String getCustomColumnForPk(String tableName) {
        return getMatchingTables(tableName)
                .map(this::extractColumnValue)
                .reduce((current, last) -> last).orElse(PK_DEFAULT_COLUMN_NAME);
    }

    private Stream<String> getMatchingTables(final String tableName) {
        return getYamlConfig()
                .<String>digNode(SECTION_TABLES).orElse(Collections.emptyMap())
                .keySet().stream()
                .map(section -> SECTION_TABLES + ":" + section)
                .filter(section -> tableMatches(section, tableName, SECTION_APPLIES_TO));
    }

    private String extractColumnValue(String section) {
        return getYamlConfig()
                .<String>digValue(section, SECTION_PRIMARY_KEY, SECTION_COLUMN)
                .orElse(PK_DEFAULT_COLUMN_NAME);
    }

    private boolean extractGenerateValue(String section) {
        return getYamlConfig().digRequiredValue(section, SECTION_PRIMARY_KEY, SECTION_GENERATE);
    }
}
