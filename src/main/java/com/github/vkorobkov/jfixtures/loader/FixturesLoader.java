package com.github.vkorobkov.jfixtures.loader;

import com.github.vkorobkov.jfixtures.config.structure.Root;
import com.github.vkorobkov.jfixtures.util.YmlUtil;
import lombok.AllArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.vkorobkov.jfixtures.util.StringUtil.cutOffExtension;
import static com.github.vkorobkov.jfixtures.util.YmlUtil.YAML_EXT;
import static com.github.vkorobkov.jfixtures.util.YmlUtil.YML_EXT;

@AllArgsConstructor
public class FixturesLoader {
    private final String path;
    private final Root config;

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
        Path relativePath = Paths.get(path).relativize(file);
        checkDotsInFolder(relativePath.getParent());
        String justFile = cutOffExtension(relativePath).toString();
        checkDotsInFile(file, justFile);

        return justFile.replace(separator, ".");
    }

    private String getFileName(Path path) {
        return path.getFileName().toString();
    }

    private void checkTwin(Path path) {
        if (YmlUtil.hasTwin(path)) {
            throw new LoaderException("Fixture exists with both extensions(yaml/yml).");
        }
    }

    private void checkDotsInFile(Path file, String relativePath) {
        if (relativePath.contains(".")) {
            String message = "Do not use dots in file names. Use nested folders instead. Wrong fixture: " + file;
            throw new LoaderException(message);
        }
    }

    private void checkDotsInFolder(Path folder) {
        if (folder != null && folder.toString().contains(".")) {
            String message = "Do not use dots in folder names. Wrong fixture folder: " + folder;
            throw new LoaderException(message);
        }
    }
}
