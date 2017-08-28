package com.github.vkorobkov.jfixtures.config;


import com.github.vkorobkov.jfixtures.config.structure.Root;
import com.github.vkorobkov.jfixtures.config.yaml.Node;
import com.github.vkorobkov.jfixtures.util.YmlUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class ConfigLoader {
    private static final String CONF_YML = ".conf.yml";
    private final Path path;

    public ConfigLoader(String fixturesRoot) {
        this.path = Paths.get(fixturesRoot).resolve(CONF_YML);
    }

    public Root load() {
        return new Root(loadRootNode());
    }

    private Node loadRootNode() {
        try {
            return Node.root(YmlUtil.load(path));
        } catch (IOException exception) {
            log.warn("file '.cong.yml' not found, using defaults");
            return Node.emptyRoot();
        }
    }
}
