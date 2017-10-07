package com.github.vkorobkov.jfixtures.processor;

import com.github.vkorobkov.jfixtures.config.structure.Root;
import com.github.vkorobkov.jfixtures.instructions.Instruction;
import com.github.vkorobkov.jfixtures.loader.Fixture;
import lombok.Getter;

import java.util.*;

@Getter
class Context {
    private final List<Instruction> instructions = new ArrayList<>();
    private final RowsIndex rowsIndex = new RowsIndex();
    private final Set<String> completedFixtures = new HashSet<>();
    private final CircularPreventer circularPreventer = new CircularPreventer();
    private final Map<String, Fixture> fixtures;
    private final Root config;

    Context(Map<String, Fixture> fixtures, Root config) {
        this.fixtures = Collections.unmodifiableMap(fixtures);
        this.config = config;
    }
}
