package files;

import java.nio.file.Path;

public class FileUtil {
    /// simply util for files

    public static String getExtension(Path path) {
        String name = path.getFileName().toString();
        String[] parts = name.split("\\.");

        if (parts.length < 2) return "";

        return parts[parts.length - 1];
    }
}
