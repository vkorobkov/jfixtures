package com.github.vkorobkov.jfixtures.loader

import com.github.vkorobkov.jfixtures.domain.Row
import com.github.vkorobkov.jfixtures.domain.Value
import nl.jqno.equalsverifier.EqualsVerifier
import spock.lang.Specification

class RowTest extends Specification {
    Map<String, Value> columns

    void setup() {
        columns = [
            id: Value.of(1),
            name: Value.of("Vladimir"),
        ]
    }

    def "constructor test"() {
        when:
        def row = new Row("vlad", columns)

        then:
        row.name == "vlad"
        row.columns == columns
    }

    def "#constuctor may accept either Value or Object as column values"() {
        given:
        def columns = [
            id: Value.of(1),
            name: "Vlad",
            age: 30
        ]

        when:
        def row = new Row("vlad", columns)

        then:
        row.columns == [
            id: Value.of(1),
            name: Value.of("Vlad"),
            age: Value.of(30)
        ]
    }

    def "columns is a read only collection"() {
        given:
        def row = new Row("vlad", columns)

        when:
        row.columns.remove("id")

        then:
        thrown(UnsupportedOperationException)
    }

    def "#withBaseColumns adds base columns"() {
        given:
        def row = new Row("vlad", columns)

        when:
        def extendedRow = row.withBaseColumns(age: 30)

        then:
        extendedRow.columns == columns + [age: Value.of(30)]

        and:
        !extendedRow.is(row)
    }

    def "#withBaseColumns keeps the original row name"() {
        given:
        def row = new Row("vlad", columns)

        when:
        def extendedRow = row.withBaseColumns(age: 30)

        then:
        extendedRow.name == "vlad"
    }

    def "#withBaseColumns does not overwrite the existing columns"() {
        given:
        def row = new Row("vlad", columns)

        when:
        def extendedRow = row.withBaseColumns(id: 100)

        then:
        extendedRow.columns == columns
    }

    def "#withBaseColumns return the same object(this) base columns is an empty map"() {
        given:
        def row = new Row("vlad", columns)

        when:
        def extendedRow = row.withBaseColumns([:])

        then:
        extendedRow.is(row)
    }

    def "equals"() {
        expect:
        EqualsVerifier.forClass(Row).verify()
    }
}
