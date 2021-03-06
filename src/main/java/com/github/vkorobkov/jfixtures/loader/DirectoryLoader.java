package com.github.vkorobkov.jfixtures.loader;

import com.github.vkorobkov.jfixtures.domain.Table;
import com.github.vkorobkov.jfixtures.util.YmlUtil;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.vkorobkov.jfixtures.util.StringUtil.cutOffExtension;
import static com.github.vkorobkov.jfixtures.util.YmlUtil.YAML_EXT;
import static com.github.vkorobkov.jfixtures.util.YmlUtil.YML_EXT;

@AllArgsConstructor
public class DirectoryLoader {
    private final String path;

    public Collection<Table> load() {
        try {
            return Files
                    .walk(Paths.get(path))
                    .filter(this::isFile)
                    .filter(this::isYml)
                    .filter(this::isNotConfig)
                    .peek(this::checkTwin)
                    .map(this::loadTable)
                    .collect(Collectors.toList());
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

    private Table loadTable(Path file) {
        String name = getTableName(file);
        Map<String, Object> yamlContent = YmlUtil.load(file);
        return Table.of(name, MapDataLoader.loadRows(yamlContent));
    }

    private String getTableName(Path file) {
        String separator = file.getFileSystem().getSeparator();
        Path relativePath = Paths.get(path).relativize(file);
        checkDotsInDirectory(Optional.ofNullable(relativePath.getParent()));
        String justFile = cutOffExtension(relativePath).toString();
        checkDotsInFile(file, justFile);

        return justFile.replace(separator, ".");
    }

    private String getFileName(Path path) {
        return path.getFileName().toString();
    }

    private void checkTwin(Path path) {
        if (YmlUtil.hasTwin(path)) {
            throw new LoaderException("File " + path + " exists with both extensions(yaml/yml).");
        }
    }

    private void checkDotsInFile(Path file, String relativePath) {
        if (relativePath.contains(".")) {
            String message = "Do not use dots in file names. Use nested directorys instead. Wrong fixture: " + file;
            throw new LoaderException(message);
        }
    }

    private void checkDotsInDirectory(Optional<Path> directory) {
        if (directory.isPresent() && directory.toString().contains(".")) {
            String message = "Do not use dots in directory names. Wrong fixture directory: " + directory.get();
            throw new LoaderException(message);
        }
    }
}
