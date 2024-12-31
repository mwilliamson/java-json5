package org.zwobble.json5.parser;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Json5ParserExternalTests {
    @TestFactory
    public Stream<DynamicTest> tests() throws IOException {
        var json5TestsPath = Path.of("json5-tests");
        return testsUnderPath(json5TestsPath, json5TestsPath);
    }

    private Stream<DynamicTest> testsUnderPath(Path rootPath, Path path) throws IOException {
        var file = path.toFile();
        if (file.isFile()) {
            var fileName = path.getFileName().toString();

            var lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex == -1) {
                return Stream.empty();
            }

            var relativePath = rootPath.relativize(path);

            var extension = fileName.substring(lastDotIndex + 1);
            switch (extension) {
                case "json":
                case "json5":
                    return Stream.of(DynamicTest.dynamicTest(
                        relativePath.toString(),
                        () -> {
                            Json5Parser.parseText(Files.readString(path));
                        }
                    ));

                case "js":
                case "txt":
                    return Stream.of(DynamicTest.dynamicTest(
                        relativePath.toString(),
                        () -> {
                            assertThrows(
                                Json5ParseError.class,
                                () -> Json5Parser.parseText(Files.readString(path))
                            );
                        }
                    ));

                default:
                    return Stream.empty();
            }
        } else if (file.isDirectory()) {
            return Files.list(path)
                .flatMap(subpath -> {
                    try {
                        return testsUnderPath(rootPath, subpath);
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    }
                });
        } else {
            return Stream.empty();
        }
    }
}
