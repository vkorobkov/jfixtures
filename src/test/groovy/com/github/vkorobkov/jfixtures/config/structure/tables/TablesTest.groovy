package com.github.vkorobkov.jfixtures.config.structure.tables

import com.github.vkorobkov.jfixtures.config.yaml.Node
import spock.lang.Specification

class TablesTest extends Specification {
    def SAMPLE_CONFIG = [
            table_NOT_have_pk         : [
                    applies_to: "friends",
                    pk        : [generate: false],
            ],
            users_related_table_has_pk: [
                    applies_to: "users",
                    pk        : [generate: true],
            ]
    ]

    def SAMPLE_CONFIG_REGEXP = [
            tables_NOT_have_pk: [
                    applies_to: "/.+",
                    pk        : [generate: false],
            ],
            table_has_pk      : [
                    applies_to: "users",
                    pk        : [generate: true],
            ]
    ]

    def SAMPLE_CONFIG_WITH_CUSTOM_PK = [
            table_has_pk: [
                    applies_to: "users",
                    pk        : [generate: true, column: "custom_id"],
            ]
    ]

    def CLEAN_UP_CONFIG = [
            no_clean_up: [
                    applies_to  : "users",
                    clean_method: "none",
            ],
            clean_up   : [
                    applies_to  : "friends",
                    clean_method: "delete",
            ]
    ]

    def CLEAN_UP_CONFIG_REGEXP = [
            no_clean_up : [
                    applies_to  : "/.+",
                    clean_method: "none",
            ],
            table_has_pk: [
                    applies_to  : "users",
                    clean_method: "delete",
            ]
    ]

    def BEFORE_INSERTS_CONFIG = [
            transactional_users: [
                    applies_to  : "users",
                    before_inserts: "BEGIN TRANSACTION;",
            ],
            transactional_friends   : [
                    applies_to  : ["friends", "mates"],
                    before_inserts: ["// Doing table \$TABLE_NAME", "BEGIN TRANSACTION;"],
            ]
    ]

    def AFTER_INSERTS_CONFIG = [
            transactional_users: [
                    applies_to  : "users",
                    after_inserts: "COMMIT TRANSACTION;",
            ],
            transactional_friends   : [
                    applies_to  : ["friends", "mates"],
                    after_inserts: ["// Completed table \$TABLE_NAME", "COMMIT TRANSACTION;"],
            ]
    ]

    def "shouldAutoGeneratePk returns true by default"() {
        expect:
        shouldAutoGeneratePk(SAMPLE_CONFIG, "mates")
    }

    def "should auto generate PK when generate flag has true value"() {
        expect:
        shouldAutoGeneratePk(SAMPLE_CONFIG, "users")
        shouldAutoGeneratePk(SAMPLE_CONFIG_REGEXP, "users")
    }

    def "should not auto generate PK when generate flag has false value"() {
        expect:
        !shouldAutoGeneratePk(SAMPLE_CONFIG, "friends")
        !shouldAutoGeneratePk(SAMPLE_CONFIG_REGEXP, "any_table")
    }

    def "should auto generate PK with default column name when generate flag does not exist"() {
        expect:
        getCustomColumnForPk(SAMPLE_CONFIG, "mates") == "id"
    }

    def "should auto generate PK with default column name when generate flag has true value"() {
        expect:
        getCustomColumnForPk(SAMPLE_CONFIG_REGEXP, "users") == "id"
    }


    def "should generate PK with custom column name"() {
        expect:
        getCustomColumnForPk(SAMPLE_CONFIG_WITH_CUSTOM_PK, "users") == "custom_id"
    }

    def "getCleanMethod returns 'delete' by default"() {
        expect:
        getCleanMethod(SAMPLE_CONFIG, "friends") == CleanMethod.DELETE
        getCleanMethod(SAMPLE_CONFIG, "users") == CleanMethod.DELETE
    }

    def "should return clean_method if set"() {
        expect:
        getCleanMethod(CLEAN_UP_CONFIG, "users") == CleanMethod.NONE
        getCleanMethod(CLEAN_UP_CONFIG, "friends") == CleanMethod.DELETE
    }

    def "should return clean_method if set for any table"() {
        expect:
        getCleanMethod(CLEAN_UP_CONFIG_REGEXP, "any_table") == CleanMethod.NONE
        getCleanMethod(CLEAN_UP_CONFIG_REGEXP, "users") == CleanMethod.DELETE
    }

    def "getBeforeInserts returns 'empty value' by default"() {
        expect:
        getBeforeInserts(SAMPLE_CONFIG, "users") == []
    }

    def "should return before_inserts if set"() {
        expect:
        getBeforeInserts(BEFORE_INSERTS_CONFIG, "users") == ["BEGIN TRANSACTION;"]
        getBeforeInserts(BEFORE_INSERTS_CONFIG, "friends") == ["// Doing table \$TABLE_NAME", "BEGIN TRANSACTION;"]
        getBeforeInserts(BEFORE_INSERTS_CONFIG, "mates") == ["// Doing table \$TABLE_NAME", "BEGIN TRANSACTION;"]
    }

    def "getAfterInserts returns 'empty value' by default"() {
        expect:
        getAfterInserts(SAMPLE_CONFIG, "users") == []
    }

    def "should return after_inserts if set"() {
        expect:
        getAfterInserts(AFTER_INSERTS_CONFIG, "users") == ["COMMIT TRANSACTION;"]
        getAfterInserts(AFTER_INSERTS_CONFIG, "friends") == ["// Completed table \$TABLE_NAME", "COMMIT TRANSACTION;"]
        getAfterInserts(AFTER_INSERTS_CONFIG, "mates") == ["// Completed table \$TABLE_NAME", "COMMIT TRANSACTION;"]
    }

    private static def shouldAutoGeneratePk(content, String name) {
        getTablesConfig(content, name).shouldAutoGeneratePk()
    }

    private static def getCustomColumnForPk(content, String name) {
        getTablesConfig(content, name).getPkColumnName()
    }

    private static def getCleanMethod(content, String name) {
        getTablesConfig(content, name).getCleanMethod()
    }

    private static def getBeforeInserts(content, String name) {
        getTablesConfig(content, name).getBeforeInserts()
    }

    private static def getAfterInserts(content, String name) {
        getTablesConfig(content, name).getAfterInserts()
    }

    private static def getTablesConfig(content, String name) {
        new Tables(Node.root(content), name)
    }
}
