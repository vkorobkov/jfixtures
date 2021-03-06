package com.github.vkorobkov.jfixtures.instructions

import com.github.vkorobkov.jfixtures.domain.Value
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import spock.lang.Specification

class InsertRowTest extends Specification {
    def "default constructor test"() {
        expect:
        new InsertRow()
    }

    def "constructor test"() {
        given:
        def values = [
            "id": Value.of(5),
            "name": Value.of("Vlad")
        ]

        when:
        def row = new InsertRow("users", "vlad", values)

        then:
        row.table == "users"
        row.rowName == "vlad"

        and:
        row.values.id.value == 5
        row.values.name.value == "Vlad"
    }

    def "values collection is immutable"() {
        given:
        def values = [
            "id": Value.of(5),
            "name": Value.of("Vlad")
        ]

        when:
        def row = new InsertRow("users", "vlad", values)
        row.values.remove("id")

        then:
        thrown(UnsupportedOperationException)
    }

    def "value toString() dummy test"() {
        expect:
        Value.of("5").toString() == Value.of("5").toString()
    }

    def "visitor accepts instruction"() {
        given:
        def visitor = Mock(InstructionVisitor)
        def instruction = new InsertRow("users", "vlad", [:])

        when:
        instruction.accept(visitor)

        then:
        1 * visitor.visit(instruction)
    }

    def "::equals"() {
        expect:
        EqualsVerifier
            .forClass(InsertRow)
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
            .verify()
    }
}
