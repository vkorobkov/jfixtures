package com.github.vkorobkov.jfixtures.config;


import com.github.vkorobkov.jfixtures.config.structure.Root;
import com.github.vkorobkov.jfixtures.config.yaml.Node;
import com.github.vkorobkov.jfixtures.util.YmlUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public final class ConfigLoader {
    private static final String CONF_YML = ".conf.yml";
    private static final String CONF_YAML = ".conf.yaml";

    private ConfigLoader() {
    }

    public static Root load(String fixturesRoot) {
        return new Root(loadRootNode(fixturesRoot));
    }

    private static Node loadRootNode(String fixturesRoot) {
        try {
            val path = getConfigPath(fixturesRoot).get();
            return Node.root(YmlUtil.load(path));
        } catch (NoSuchElementException e) {
            log.info("Neither file '" + CONF_YAML + "' nor '" + CONF_YML + "' not found, using defaults");
        } catch (IOException e) {
            log.warn("Config loading failed, using the defaults. Details: " + e);
        }
        return Node.emptyRoot();
    }

    private static Optional<Path> getConfigPath(String fixturesRoot) {
        return Stream
                .of(CONF_YAML, CONF_YML)
                .map(Paths.get(fixturesRoot)::resolve)
                .filter(Files::exists)
                .peek(ConfigLoader::checkTwin)
                .findFirst();
    }

    private static void checkTwin(Path path) {
        if (YmlUtil.hasTwin(path)) {
            throw new ConfigLoaderException("Fixture's config exists with both extensions(yaml/yml).");
        }
    }
}
