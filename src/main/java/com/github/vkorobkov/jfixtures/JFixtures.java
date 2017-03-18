package com.github.vkorobkov.jfixtures;

import com.github.vkorobkov.jfixtures.fluent.JFixturesResult;
import com.github.vkorobkov.jfixtures.fluent.JFixturesResultImpl;
import com.github.vkorobkov.jfixtures.sql.PgSql;

public class JFixtures {
    static JFixturesResult postgres(String fixturesFolder) {
        return new JFixturesResultImpl(fixturesFolder, new PgSql());
    }
}
