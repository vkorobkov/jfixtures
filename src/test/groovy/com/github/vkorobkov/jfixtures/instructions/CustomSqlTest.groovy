package com.github.vkorobkov.jfixtures.instructions

import spock.lang.Specification

class CustomSqlTest extends Specification {
    def "constructor test"() {
        when:
        CustomSql customSql = new CustomSql("users", "BEGIN TRANSACTION;")

        then:
        customSql.table == "users"
        customSql.instruction == "BEGIN TRANSACTION;"
    }

    def "should replace table name placeholder"() {
        when:
        CustomSql customSql = new CustomSql("users", "// Doing table \$TABLE_NAME")

        then:
        customSql.table == "users"
        customSql.instruction == "// Doing table users"
    }

    def "visitor accepts instruction"() {
        given:
        def visitor = Mock(InstructionVisitor)
        def instruction = new CustomSql("users", "BEGIN TRANSACTION;")

        when:
        instruction.accept(visitor)

        then:
        1 * visitor.visit(instruction)
    }
}
