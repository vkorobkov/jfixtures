package com.github.vkorobkov.jfixtures.loader

import com.github.vkorobkov.jfixtures.testutil.WithTestResource
import spock.lang.Specification

import java.nio.file.NoSuchFileException

class YmlRowsLoaderTest extends Specification implements WithTestResource {

    def "load simple fixture test"() {
        given:
        def path = testResourcePath("simple_rows.yml")

        when:
        def fixtures = new YmlRowsLoader(path).get()

        then:
        fixtures.size() == 2

        and:
        def vlad = fixtures[0]
        vlad.name == "vlad"
        vlad.columns.first_name == FixtureValue.ofAuto("Vladimir")
        vlad.columns.age == FixtureValue.ofAuto(29)
        vlad.columns.sex == FixtureValue.ofAuto("man")

        and:
        def dima = fixtures[1]
        dima.name == "diman"
        dima.columns.first_name == FixtureValue.ofAuto("Dmitry")
        dima.columns.age == FixtureValue.ofAuto(28)
        dima.columns.sex == FixtureValue.ofAuto("man")
    }

    def "columns remain the ordering"() {
        given:
        def path = testResourcePath("simple_rows.yml")

        when:
        def fixtures = new YmlRowsLoader(path).get()

        then:
        def columnsKeys = fixtures[0].columns.keySet().asList()
        columnsKeys == ["first_name", "age", "sex"]
    }

    def "takes the last column value when column duplicates"() {
        given:
        def path = testResourcePath("duplicate_columns.yml")

        when:
        def fixtures = new YmlRowsLoader(path).get()

        then:
        fixtures[0].columns.age == FixtureValue.ofAuto(35)
    }

    def "throws when file does not exist"() {
        given:
        def path = notExistingPath(".yml")

        when:
        new YmlRowsLoader(path).get()

        then:
        def exception = thrown(LoaderException)
        exception.cause instanceof NoSuchFileException
    }

    def "no rows when loads an empty fixture"() {
        given:
        def path = testResourcePath("empty_fixture.yml")

        when:
        def fixtures = new YmlRowsLoader(path).get()

        then:
        fixtures.empty
    }

    def "no columns when row is empty in the fixture"() {
        given:
        def path = testResourcePath("with_empty_row.yml")

        when:
        def fixtures = new YmlRowsLoader(path).get()

        then:
        fixtures.size() == 2

        and:
        def vlad = fixtures[0]
        vlad.name == "vlad"
        vlad.columns.first_name == FixtureValue.ofAuto("Vladimir")
        vlad.columns.age == FixtureValue.ofAuto(29)
        vlad.columns.sex == FixtureValue.ofAuto("man")

        and:
        def dima = fixtures[1]
        dima.name == "diman"
        dima.columns.isEmpty()
    }

    def "loads explicitely defined SQL type and explicitely auto type"() {
        given:
        def path = testResourcePath("custom_row_types.yml")

        when:
        def fixtures = new YmlRowsLoader(path).get()

        then:
        def vlad = fixtures.first()
        vlad.name == "vlad"
        vlad.columns.first_name == FixtureValue.ofAuto("Vladimir")
        vlad.columns.age == FixtureValue.ofSql("DEFAULT")
    }
}
