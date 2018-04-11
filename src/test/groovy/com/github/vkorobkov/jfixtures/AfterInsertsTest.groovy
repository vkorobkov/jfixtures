package com.github.vkorobkov.jfixtures

import com.github.vkorobkov.jfixtures.testutil.YamlVirtualFolder
import spock.lang.Specification

class AfterInsertsTest extends Specification implements YamlVirtualFolder {
    def DEFAULT_EXPECTED_SQL = """DELETE FROM "users";
            |INSERT INTO "users" ("id", "name", "age") VALUES (${IntId.one('vlad')}, 'Vladimir', 29);
            |""".stripMargin()
    def CUSTOM_EXPECTED_SQL = """DELETE FROM "users";
            |INSERT INTO "users" ("id", "name", "age") VALUES (${IntId.one('vlad')}, 'Vladimir', 29);
            |// Completed table users
            |COMMIT TRANSACTION;
            |""".stripMargin()

    def "should not insert any custom SQL by default"() {
        setup:
        def folder = unpackYamlToTempFolder("default.yml")

        expect:
        JFixturesOld.sql99(folder as String).asString() == DEFAULT_EXPECTED_SQL

        cleanup:
        folder.toFile().deleteDir()
    }

    def "should insert custom SQL if present"() {
        setup:
        def folder = unpackYamlToTempFolder("custom.yml")

        expect:
        JFixturesOld.sql99(folder as String).asString() == CUSTOM_EXPECTED_SQL

        cleanup:
        folder.toFile().deleteDir()
    }
}
