package com.github.vkorobkov.jfixtures.loader;

import com.github.vkorobkov.jfixtures.config.structure.Root;
import com.github.vkorobkov.jfixtures.util.YmlUtil;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.vkorobkov.jfixtures.util.YmlUtil.YAML_EXT;
import static com.github.vkorobkov.jfixtures.util.YmlUtil.YML_EXT;

public class FixturesLoader {
    private final String path;
    private final Root config;

    public FixturesLoader(String path, Root config) {
        this.path = path;
        this.config = config;
    }

    public Map<String, Fixture> load() {
        try {
            return Files
                    .walk(Paths.get(path))
                    .filter(this::isFile)
                    .filter(this::isYml)
                    .filter(this::isNotConfig)
                    .peek(this::checkTwin)
                    .map(this::loadFixture)
                    .collect(Collectors.toMap(fixture -> fixture.name, fixture -> fixture));
        } catch (IOException cause) {
            String message = "Can not load fixtures from directory: " + path;
            throw new LoaderException(message, cause);
        }
    }

    private boolean isFile(Path file) {
        return !Files.isDirectory(file);
    }

    private boolean isYml(Path path) {
        return Stream
                .of(YAML_EXT, YML_EXT)
                .anyMatch(ext -> getFileName(path).endsWith(ext));
    }

    private boolean isNotConfig(Path path) {
        return !getFileName(path).startsWith(".");
    }

    private Fixture loadFixture(Path file) {
        String name = getFixtureName(file);
        val baseColumns = config.columns().forTable(name);
        return new Fixture(name, new YmlRowsLoader(file, baseColumns));
    }

    private String getFixtureName(Path file) {
        String separator = file.getFileSystem().getSeparator();
        String relativePath = Paths.get(path).relativize(file).toString();

        return Stream
                .of(YAML_EXT, YML_EXT)
                .filter(relativePath::endsWith)
                .map(ext -> relativePath.substring(0, relativePath.lastIndexOf(ext)))
                .peek(path -> checkDot(file, path))
                .findFirst().get()
                .replace(separator, ".");
    }

    private String getFileName(Path path) {
        return path.getFileName().toString();
    }

    private void checkTwin(Path path) {
        if (YmlUtil.hasTwin(path)) {
            throw new LoaderException("Fixture exists with both extensions(yaml/yml).");
        }
    }

    private void checkDot(Path file, String relativePath) {
        if (relativePath.contains(".")) {
            String message = "Do not use dots in file names. Use nested folders instead. Wrong fixture: " + file;
            throw new LoaderException(message);
        }
    }
}
