package bank.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class FileStore {

    private FileStore() {}

    public static void ensureDirectory(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create data directory: " + dir, e);
        }
    }

    public static <T> void saveAll(Path filePath, List<T> data, Function<T, String> toStringFunction) {
        List<String> lines = new ArrayList<>();
        for (T item : data) {
            lines.add(toStringFunction.apply(item));
        }
        try {
            Files.write(filePath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write " + filePath, e);
        }
    }

    public static List<String> loadAll(Path filePath) {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();          // first run: no file yet, empty bank
        }
        try {
            return Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read " + filePath, e);
        }
    }

    public static void appendToFile(Path filePath, String line) {
        try {
            Files.write(filePath, List.of(line), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Cannot append to " + filePath, e);
        }
    }
}