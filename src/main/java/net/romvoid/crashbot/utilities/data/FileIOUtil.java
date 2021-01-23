package net.romvoid.crashbot.utilities.data;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileIOUtil {
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    public static String read(Path path) throws IOException {
        return Files.readString(path, UTF8);
    }

    public static void write(Path path, String contents) throws IOException {
        Files.writeString(path, contents, UTF8);
    }
}
