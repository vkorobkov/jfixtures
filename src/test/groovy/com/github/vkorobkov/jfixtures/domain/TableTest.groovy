package com.github.vkorobkov.jfixtures.domain

import spock.lang.Specification

class TableTest extends Specification {
    Collection<Row> rows

    void setup() {
        rows = [
                Row.ofName("Vlad"),
                Row.ofName("Bob"),
                Row.ofName("Ned"),
        ]
    }

    def "returns rows provided by static method"() {
        when:
        def table = Table.of("users", rows)

        then:
        table.rows.asList() == rows
    }

    def "rows is a read only collection"() {
        when:
        def fixture = Table.of("users", rows)
        fixture.rows.remove("Vlad")

        then:
        thrown(UnsupportedOperationException)
    }

    def "rows returns the same collection every call"() {
        when:
        def fixture = Table.of("users", rows)

        then:
        fixture.name == "users"
        fixture.rows.is(fixture.rows)
    }

    def "#mergeRows adds new rows to the end"() {
        given:
        def fixture = Table.of("users",
                [Row.of("Vlad", [age: 30]), Row.of("Mr Burns", [age: 100])]
        )

        when:
        def merged = fixture.mergeRows([
                Row.of("Homer", [age: 50]),
                Row.of("Bart", [age: 12])
        ])

        then:
        merged.rows.asList() == [
                Row.of("Vlad", [age: 30]),
                Row.of("Mr Burns", [age: 100]),
                Row.of("Homer", [age: 50]),
                Row.of("Bart", [age: 12])
        ]
    }

    def "#mergeRows replaces rows with the same name"() {
        given:
        def fixture = Table.of("users",
                [Row.of("Vlad", [age: 29]), Row.of("Mr Burns", [age: 100])]
        )

        when:
        def merged = fixture.mergeRows([
                Row.of("Homer", [age: 50]),
                Row.of("Vlad", [age: 30, skill: "java"])
        ])

        then:
        merged.rows.asList() == [
                Row.of("Vlad", [age: 30, skill: "java"]),
                Row.of("Mr Burns", [age: 100]),
                Row.of("Homer", [age: 50])
        ]
    }

    def "#mergeRows returns original rows if rows to merge is an empty list"() {
        given:
        def fixture = Table.of("users",
                [Row.of("Vlad", [age: 30]), Row.of("Mr Burns", [age: 100])]
        )

        when:
        def merged = fixture.mergeRows([])

        then:
        merged.rows.asList() == [
                Row.of("Vlad", [age: 30]),
                Row.of("Mr Burns", [age: 100])
        ]
    }

    def "#mergeRows returns fixture with original name"() {
        given:
        def fixture = Table.of("users",
                [Row.of("Vlad", [age: 29]), Row.of("Mr Burns", [age: 100])]
        )

        when:
        def merged = fixture.mergeRows([
                Row.of("Homer", [age: 50]),
                Row.of("Vlad", [age: 30, skill: "java"])
        ])

        then:
        merged.name == "users"
    }

    def "#mergeTables puts fixtures for different tables in sequence"() {
        given:
        def users = Table.of("users", [
                Row.of("Vlad", [name: "Vlad", age: 30]),
                Row.of("Burns", [name: "Mr Burns", age: 130])
        ])
        def roles = Table.of("roles", [
                Row.of("user", [type: "user", readAccess: true, writeAccess: false]),
                Row.of("admin", [type: "admin", readAccess: true, writeAccess: true])
        ])

        when:
        def merged = Table.mergeTables([users, roles])

        then:
        merged.size() == 2

        and:
        merged[0] == users
        merged[1] == roles
    }

    def "#mergeTables concatenates different rows of the same table"() {
        given:
        def users1 = Table.of("users", [
                Row.of("Vlad", [name: "Vlad", age: 30]),
                Row.of("Burns", [name: "Mr Burns", age: 130])
        ])
        def users2 = Table.of("users", [
                Row.of("Homer", [name: "Homer", age: 40]),
                Row.of("Bart", [name: "Bart", age: 12])
        ])

        when:
        def merged = Table.mergeTables([users1, users2])

        then:
        merged.size() == 1

        and:
        with(merged[0]) {
            name == "users"
            rows.toList() == users1.rows + users2.rows
        }
    }

    def "#mergeTables replaces old rows with new ones in scope of the same table"() {
        given:
        def users1 = Table.of("users", [
                Row.of("Vlad", [name: "Vlad", age: 29]),
                Row.of("Burns", [name: "Mr Burns", age: 130])
        ])
        def users2 = Table.of("users", [
                Row.of("Vlad", [name: "Vladimir", age: 30])
        ])

        when:
        def merged = Table.mergeTables([users1, users2])

        then:
        merged.size() == 1

        and:
        with(merged[0]) {
            name == "users"
            rows.toList() == [
                    Row.of("Vlad", [name: "Vladimir", age: 30]), Row.of("Burns", [name: "Mr Burns", age: 130])
            ]
        }
    }

    def "::ofName creates a new empty table"() {
        when:
        def table = Table.ofName("users")

        then:
        with(table) {
            name == "users"
            rows.size() == 0
        }
    }

    def "::of(Row...) creates a new table"() {
        when:
        def table = Table.of("users", Row.ofName("Homer"), Row.ofName("Bart"))

        then:
        with(table) {
            name == "users"
            rows[0].name == "Homer"
            rows[1].name == "Bart"
        }
    }

    def "::of(Collection<Row>) creates a new table"() {
        when:
        def table = Table.of("users", [Row.ofName("Homer"), Row.ofName("Bart")])

        then:
        with(table) {
            name == "users"
            rows[0].name == "Homer"
            rows[1].name == "Bart"
        }
    }

    def "::ofRow(columns) creates a new table"() {
        when:
        def table = Table.ofRow("users", "Homer", [age : 39])

        then:
        with(table) {
            name == "users"
            rows[0].name == "Homer"
            rows[0].columns.toMapString() == [age: Value.of(39)].toMapString()
        }
    }

    def "::ofRow(keyValuePairs) creates a new table"() {
        when:
        def table = Table.ofRow("users", "Homer", "age", 39)

        then:
        with(table) {
            name == "users"
            rows[0].name == "Homer"
            rows[0].columns.toMapString() == [age: Value.of(39)].toMapString()
        }
    }

    def "::addRows(Row...) adds rows to existing table"() {
        given:
        def table = Table.ofName("users")

        when:
        def tableWithRows = table.addRows(Row.ofName("Bart"), Row.ofName("Lisa"))

        then:
        with(tableWithRows) {
            name == "users"
            rows[0].name == "Bart"
            rows[1].name == "Lisa"
        }

        and:
        !tableWithRows.is(table)
    }

    def "::addRows(Row...) does not add duplicated rows to existing table"() {
        given:
        def table = Table.of("users", Row.ofName("Bart"))

        when:
        def tableWithRows = table.addRows(Row.ofName("Bart"))

        then:
        with(tableWithRows) {
            name == "users"
            rows[0].name == "Bart"
            rows.size() == 1
        }

        and:
        !tableWithRows.is(table)
    }


    def "::addRows(Collection<Row>) adds rows to existing table"() {
        given:
        def table = Table.ofName("users")

        when:
        def tableWithRows = table.addRows([Row.ofName("Bart"), Row.ofName("Lisa")])

        then:
        with(tableWithRows) {
            name == "users"
            rows[0].name == "Bart"
            rows[1].name == "Lisa"
        }

        and:
        !tableWithRows.is(table)
    }

    def "::addRows(Collection<Row>) does not add duplicated rows to existing table"() {
        given:
        def table = Table.of("users", Row.ofName("Bart"))

        when:
        def tableWithRows = table.addRows([Row.ofName("Bart")])

        then:
        with(tableWithRows) {
            name == "users"
            rows[0].name == "Bart"
            rows.size() == 1
        }

        and:
        !tableWithRows.is(table)
    }

    def "::addRow(columns) adds rows to existing table"() {
        given:
        def table = Table.ofName("users")

        when:
        def tableWithRow = table.addRow("Bart", [age: 10])

        then:
        with(tableWithRow) {
            name == "users"
            rows[0].name == "Bart"
            rows[0].columns.toMapString() == [age: Value.of(10)].toMapString()
        }

        and:
        !tableWithRow.is(table)
    }

    def "::addRow(columns) does not add duplicated row to existing table"() {
        given:
        def table = Table.of("users", Row.ofName("Bart"))

        when:
        def tableWithRow = table.addRow("Bart", [age: 10])

        then:
        with(tableWithRow) {
            name == "users"
            rows[0].name == "Bart"
            rows.size() == 1
        }

        and:
        !tableWithRow.is(table)
    }

    def "::addRow(keyValuePairs) adds rows to existing table"() {
        given:
        def table = Table.ofName("users")

        when:
        def tableWithRow = table.addRow("Bart", "age", 10)

        then:
        with(tableWithRow) {
            name == "users"
            rows[0].name == "Bart"
            rows[0].columns.toMapString() == [age: Value.of(10)].toMapString()
        }

        and:
        !tableWithRow.is(table)
    }

    def "::addRow(keyValuePairs) does not add duplicated row to existing table"() {
        given:
        def table = Table.of("users", Row.ofName("Bart"))

        when:
        def tableWithRow = table.addRow("Bart", "age", 10)

        then:
        with(tableWithRow) {
            name == "users"
            rows[0].name == "Bart"
            rows.size() == 1
        }

        and:
        !tableWithRow.is(table)
    }
}
