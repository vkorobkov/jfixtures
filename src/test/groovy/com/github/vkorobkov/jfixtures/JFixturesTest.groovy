package com.github.vkorobkov.jfixtures

import com.github.vkorobkov.jfixtures.config.structure.tables.CleanMethod
import com.github.vkorobkov.jfixtures.domain.Row
import com.github.vkorobkov.jfixtures.domain.Table
import com.github.vkorobkov.jfixtures.domain.Value
import com.github.vkorobkov.jfixtures.testutil.Assertions
import com.github.vkorobkov.jfixtures.testutil.InstructionsHelper
import com.github.vkorobkov.jfixtures.testutil.YamlVirtualDirectory
import spock.lang.Specification

import java.nio.file.Paths

class JFixturesTest extends Specification implements YamlVirtualDirectory, InstructionsHelper, Assertions {
    def "::withConfig(String) instantiates object with config path stored"() {
        expect:
        JFixtures.withConfig("path/.conf").config == Optional.of("path/.conf")
    }

    def "::withConfig(File) instantiates object with config path stored"() {
        when:
        def file = new File("path/.conf")

        then:
        JFixtures.withConfig(file).config == Optional.of(file.getAbsolutePath())
    }

    def "::withConfig(Path) instantiates object with config path stored"() {
        when:
        def path = Paths.get("path/.conf")

        then:
        JFixtures.withConfig(path).config == Optional.of(path.toString())
    }

    def "::withConfig(String) instantiates with default profile"() {
        expect:
        JFixtures.withConfig("path/.conf").profile == "default"
    }

    def "::noConfig instantiates object with empty config"() {
        expect:
        JFixtures.noConfig().config == Optional.empty()
    }

    def "::noConfig instantiates with default profile"() {
        expect:
        JFixtures.noConfig().profile == "default"
    }

    def "::withProfile replaced the profile and returns another instance"() {
        given:
        def fixtures = JFixtures.noConfig()

        when:
        def fixturesWithProfile = fixtures.withProfile("unit")

        then:
        fixturesWithProfile.profile == "unit"

        and:
        !fixturesWithProfile.is(fixtures)
    }

    def "instantiated object has read only tables collection"() {
        given:
        def tables = JFixtures.noConfig().tables

        when:
        tables.add(Table.ofName("users"))

        then:
        thrown(UnsupportedOperationException)
    }

    def "::addTables(Collection<Table>) adds new tables and returns another instance"() {
        given:
        def tablesToAdd = [
                Table.ofName("users"),
                Table.ofName("comments")
        ]
        def fixtures = JFixtures.noConfig()

        when:
        def withTables = fixtures.addTables(tablesToAdd)

        then:
        withTables.tables.toListString() == tablesToAdd.toListString()

        and:
        !withTables.is(fixtures)
    }

    def "::addTables(Collection<Table>) could be called in chain accumulating tables"() {
        given:
        def tablesToAdd = [
                Table.ofName("users"),
                Table.ofName("comments")
        ]
        def fixtures = JFixtures.noConfig()

        when:
        fixtures = fixtures.addTables(tablesToAdd).addTables(tablesToAdd)

        then:
        fixtures.tables.toListString() == (tablesToAdd + tablesToAdd).toListString()
    }

    def "::addTables(Collection<Table>) allows empty list"() {
        expect:
        JFixtures.noConfig().addTables(Collections.emptyList()).tables.size() == 0
    }

    def "::addTables(Table...) adds new tables and returns another instance"() {
        given:
        def tablesToAdd = [
                Table.ofName("users"),
                Table.ofName("comments")
        ] as Table[]
        def fixtures = JFixtures.noConfig()

        when:
        def withTables = fixtures.addTables(tablesToAdd)

        then:
        withTables.tables.toListString() == tablesToAdd.toList().toListString()

        and:
        !withTables.is(fixtures)
    }

    def "::addTables(Table...) could be called in chain accumulating tables"() {
        given:
        def tablesToAdd = [
                Table.ofName("users"),
                Table.ofName("comments")
        ] as Table[]
        def fixtures = JFixtures.noConfig()

        when:
        fixtures = fixtures.addTables(tablesToAdd).addTables(tablesToAdd)

        then:
        fixtures.tables.toListString() == (tablesToAdd.toList() + tablesToAdd.toList()).toListString()
    }

    def "::addTables(Table...) allows empty list"() {
        expect:
        JFixtures.noConfig().addTables([] as Table[]).tables.size() == 0
    }

    def "::load(String) adds fixtures stored in the directory"() {
        setup:
        def path = unpackYamlToTempDirectory("default.yml")

        when:
        def tables = JFixtures.noConfig().load(path.toString()).tables

        then:
        assertLoadedTables(tables)

        cleanup:
        path.toFile().deleteDir()
    }

    def "::load(Path) adds fixtures stored in the directory"() {
        setup:
        def path = unpackYamlToTempDirectory("default.yml")

        when:
        def tables = JFixtures.noConfig().load(path).tables

        then:
        assertLoadedTables(tables)

        cleanup:
        path.toFile().deleteDir()
    }

    def "::load(File) adds fixtures stored in the directory"() {
        setup:
        def path = unpackYamlToTempDirectory("default.yml")

        when:
        def tables = JFixtures.noConfig().load(path.toFile()).tables

        then:
        assertLoadedTables(tables)

        cleanup:
        path.toFile().deleteDir()
    }

    def "::load(String) adds fixtures stored in a single yaml file"() {
        when:
        def tables = JFixtures.noConfig().load("src/test/resources/j_fixtures_test/mono.yml").tables

        then:
        tables.size() == 2

        def users = tables[0]
        users.name == "users"
        users.rows.toList() == [
                Row.of("vlad", [id: 1, name: "Vlad", age: 30]),
                Row.of("homer", [id: 2, name: "Homer", age: 40])
        ]

        def roles = tables[1]
        roles.name == "admin.roles"
        roles.rows.toList() == [
                Row.of("admin", [reads: true, writes: true]),
                Row.of("user", [reads: true, writes: false])
        ]
    }

    def "::compile returns instructions without specified config"() {
        given:
        def rows = [
            Row.of("vlad", [name: "Vlad", age: 30, lang: "java"]),
            Row.of("homer", [name: "Homer S.", age: 40, lang: "beer"]),
        ]
        def table = Table.of("users", rows)

        when:
        def result = JFixtures.noConfig().addTables(table).compile()

        then:
        assertCollectionsEqual(result.instructions, [
            cleanTable("users"),
            insertRow("users", "vlad", [
                id: IntId.one("vlad"),
                name: "Vlad",
                age: 30,
                lang: "java"
            ]),
            insertRow("users", "homer", [
                id: IntId.one("homer"),
                name: "Homer S.",
                age: 40,
                lang: "beer"
            ])
        ])
    }

    def "::compile merges tables"() {
        given:
        def tables = [
            Table.of("users", [Row.of("vlad", [name: "Vlad", age: 30, lang: "java"])]),
            Table.of("users", [Row.of("homer", [name: "Homer S.", age: 40, lang: "beer"])])
        ]

        when:
        def result = JFixtures.noConfig().addTables(tables).compile()

        then:
        assertCollectionsEqual(result.instructions, [
            cleanTable("users"),
            insertRow("users", "vlad", [
                id: IntId.one("vlad"),
                name: "Vlad",
                age: 30,
                lang: "java"
            ]),
            insertRow("users", "homer", [
                id: IntId.one("homer"),
                name: "Homer S.",
                age: 40,
                lang: "beer"
            ])
        ])
    }

    def "::compile returns instructions with specified config file"() {
        setup:
        def path = unpackYamlToTempDirectory("default.yml")

        def rows = [
            Row.of("vlad", [name: "Vlad", age: 30, lang: "java"]),
            Row.of("homer", [name: "Homer S.", age: 40, lang: "beer"]),
        ]
        def table = Table.of("users", rows)

        when:
        def result = JFixtures.withConfig(path.resolve(".conf").toString()).addTables(table).compile()

        then:
        assertCollectionsEqual(result.instructions, [
            cleanTable("users", CleanMethod.NONE),
            insertRow("users", "vlad", [
                id: IntId.one("vlad"),
                name: "Vlad",
                age: 30,
                lang: "java"
            ]),
            insertRow("users", "homer", [
                id: IntId.one("homer"),
                name: "Homer S.",
                age: 40,
                lang: "beer"
            ])
        ])

        cleanup:
        path.toFile().deleteDir()
    }

    def "::addTables(Map<String, Map<String, Object>>) adds new tables"() {
        given:
        def tablesWithRows = [
                users   : [
                        vlad : [id: 5, age: 30],
                        homer: [id: 6, age: 39]
                ],
                comments: [:]
        ]
        def fixtures = JFixtures.noConfig()

        when:
        def withTables = fixtures.addTables(tablesWithRows).tables

        then:
        withTables.size() == 2

        and:
        def users = withTables[0]
        users.name == 'users'
        users.rows.size() == 2

        and:
        def vlad = users.rows[0]
        vlad.name == 'vlad'
        vlad.columns.id == Value.of(5)
        vlad.columns.age == Value.of(30)

        and:
        def homer = users.rows[1]
        homer.name == 'homer'
        homer.columns.id == Value.of(6)
        homer.columns.age == Value.of(39)

        and:
        def comments = withTables[1]
        comments.name == 'comments'
        comments.rows.size() == 0
    }

    private static assertLoadedTables(tables) {
        assert tables.size() == 1

        def table = tables.first()
        assert table.name == "users"
        assert table.rows.toList() == [
                Row.of("vlad", [id: 1, name: "Vlad", age: 30]),
                Row.of("homer", [id: 2, name: "Homer", age: 40])
        ]
        true
    }
}
