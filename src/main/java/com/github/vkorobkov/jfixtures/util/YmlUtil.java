package com.github.vkorobkov.jfixtures.util;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

public final class YmlUtil {
    private YmlUtil() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> load(Path file) throws IOException {
        Object loaded = new Yaml().load(Files.newInputStream(file));
        return loaded == null ? Collections.emptyMap() : (Map<String, Object>) loaded;
    }
}
