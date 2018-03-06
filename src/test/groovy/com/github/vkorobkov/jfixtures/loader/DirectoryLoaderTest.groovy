package com.github.vkorobkov.jfixtures.loader

import com.github.vkorobkov.jfixtures.testutil.YamlVirtualFolder
import spock.lang.Specification

import java.nio.file.NoSuchFileException

class DirectoryLoaderTest extends Specification implements YamlVirtualFolder {

    def "loads big fixtures tree"() {
        when:
        def fixtures = load("fixtures_tree.yml")

        then:
        fixtures.size() == 11
        def names = fixtures.keySet()

        and:
        names.containsAll(["usage_log", "sessions"])

        and:
        names.containsAll(["admin.users", "admin.permissions", "admin.registrations"])

        and:
        names.containsAll(["content.tickets", "content.comments", "content.filters"])

        and:
        names.containsAll(["search.comments", "search.descriptions", "search.wikis"])
    }

    def "fails if fixture name contains a dot"() {
        when:
        load("fixture_with_dot.yml")

        then:
        LoaderException exception = thrown()
        exception.message.startsWith("Do not use dots in file names. Use nested folders instead. Wrong fixture:")
    }

    def "fails with IOException if folder with fixtures does not exist"() {
        given:
        def path = notExistingPath() as String

        when:
        new DirectoryLoader(path).load()

        then:
        def exception = thrown(LoaderException)
        exception.cause instanceof NoSuchFileException
    }

    def "returns no fixtures if source folder is empty"() {
        when:
        def fixtures = load("empty_folder.yml")

        then:
        fixtures.isEmpty()
    }

    def "it is possible to load fixture's rows"() {
        when:
        def fixtures = load("fixtures_tree_with_content.yml")

        then:
        fixtures.size() == 11
        def users = fixtures.get("admin.users").rows

        and:
        def vlad = users[0]
        vlad.name == "vlad"
        vlad.columns.first_name == new FixtureValue("Vladimir")
        vlad.columns.age == new FixtureValue(29)
        vlad.columns.sex == new FixtureValue("man")

        and:
        def dima = users[1]
        dima.name == "diman"
        dima.columns.first_name == new FixtureValue("Dmitry")
        dima.columns.age == new FixtureValue(28)
        dima.columns.sex == new FixtureValue("man")
    }

    def "short syntax for row content is loaded well"() {
        when:
        def fixtures = load("fixture_with_short_syntax.yml")

        then:
        def users = fixtures.get("users").rows

        and:
        with(users[0]) {
            name == "vlad"
            with(columns) {
                first_name == new FixtureValue("Vladimir")
                age == new FixtureValue(29)
                sex == new FixtureValue("man")
            }
        }

        and:
        with(users[1]) {
            name == "diman"
            with(columns) {
                first_name == new FixtureValue("Dmitry")
                age == new FixtureValue(28)
                sex == new FixtureValue("man")
            }
        }
    }

    def "#load consumes fixture with .yaml extension"() {
        when:
        def fixtures = load("yaml_config.yaml")

        then:
        def users = fixtures.get("users").rows

        and:
        def vlad = users[0]
        vlad.name == "vlad"
        vlad.columns.first_name == new FixtureValue("Vladimir")
        vlad.columns.age == new FixtureValue(29)

        and:
        def dima = users[1]
        dima.name == "diman"
        dima.columns.first_name == new FixtureValue("Dmitry")
        dima.columns.age == new FixtureValue(28)
    }

    def "#load throws when a yaml fixture has a twin"() {
        when:
        load("yaml_yml.yaml")

        then:
        def e = thrown(LoaderException)
        e.message == "Fixture exists with both extensions(yaml/yml)."
    }

    def "#load throws when a fixture's parent folder name contains dots"() {
        when:
        load("fixture_parent_folder_with_dots.yml")

        then:
        LoaderException exception = thrown()
        exception.message.startsWith("Do not use dots in folder names. Wrong fixture folder: parent.folder.with.dots")
    }

    def "#load throws when a fixture's ancestor folder name contains dots"() {
        when:
        load("fixture_ancestor_folder_with_dots.yml")

        then:
        LoaderException exception = thrown()
        exception.message.startsWith("Do not use dots in folder names. Wrong fixture folder: ancestor.folder.with.dots/parentFolder")
    }

    Map<String, Fixture> load(String ymlFile) {
        def path = unpackYamlToTempFolder(ymlFile) as String
        new DirectoryLoader(path).load()
    }
}