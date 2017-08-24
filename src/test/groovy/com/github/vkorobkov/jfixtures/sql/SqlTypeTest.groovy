package com.github.vkorobkov.jfixtures.sql

import com.github.vkorobkov.jfixtures.sql.dialects.ClickHouse
import com.github.vkorobkov.jfixtures.sql.dialects.H2
import com.github.vkorobkov.jfixtures.sql.dialects.MsSql
import com.github.vkorobkov.jfixtures.sql.dialects.MySql
import com.github.vkorobkov.jfixtures.sql.dialects.OracleSql
import com.github.vkorobkov.jfixtures.sql.dialects.PgSql
import com.github.vkorobkov.jfixtures.sql.dialects.SQLiteSql
import com.github.vkorobkov.jfixtures.sql.dialects.SybaseSql
import spock.lang.Specification

class SqlTypeTest extends Specification {
    def "SqlType positive cases"(String value, expected) {
        expect:
        SqlType.valueOf(value) == expected

        where:
        value        | expected
        "POSTGRES"   | SqlType.POSTGRES
        "MYSQL"      | SqlType.MYSQL
        "H2"         | SqlType.H2
        "CLICKHOUSE" | SqlType.CLICKHOUSE
        "ORACLE"     | SqlType.ORACLE
        "MSSQL"      | SqlType.MSSQL
        "SYBASE"     | SqlType.SYBASE
        "SQLITE"     | SqlType.SQLITE
    }

    def "SqlType throws exception when wrong value is provided"() {
        when:
        SqlType.valueOf("wrong_type")

        then:
        thrown(IllegalArgumentException)
    }

    def "SqlType get sql dialects"() {
        expect:
        SqlType.POSTGRES.sqlDialect.class == PgSql
        SqlType.MYSQL.sqlDialect.class == MySql
        SqlType.H2.sqlDialect.class == H2
        SqlType.CLICKHOUSE.sqlDialect.class == ClickHouse
        SqlType.ORACLE.sqlDialect.class == OracleSql
        SqlType.MSSQL.sqlDialect.class == MsSql
        SqlType.SYBASE.sqlDialect.class == SybaseSql
        SqlType.SQLITE.sqlDialect.class == SQLiteSql
    }
}
