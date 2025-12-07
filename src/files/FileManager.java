package files;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final Path mainDir = Path.of(System.getProperty("user.home"), "Documents", "p2p-refactor");
    private static final Path keyDir = mainDir.resolve("keys");
    private static final Path trustedKeysDir = keyDir.resolve("trusted-keys");
    private static final Path logFile = mainDir.resolve("latest.log");

    public static void init() {
        try {
            List<Path> dirs = List.of(
                    mainDir,
                    keyDir,
                    trustedKeysDir
            );
            for (Path dir : dirs) {
                Files.createDirectories(dir);
            }

            if (Files.exists(logFile)) Files.delete(logFile);
            Files.createFile(logFile);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void writeToLogFile(String text) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                logFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            writer.write(text);
            writer.newLine();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean identityKeysExist() {
        Path publicKeyFile = keyDir.resolve("public.key");
        Path privateKeyFile = keyDir.resolve("private.key");
        return Files.exists(publicKeyFile) && Files.exists(privateKeyFile);
    }

    public static void setIdentityKeys(byte[] encodedPublicKey, byte[] encodedPrivateKey) {
        Path publicKeyFile = keyDir.resolve("public.key");
        Path privateKeyFile = keyDir.resolve("private.key");

        class Helper {
            private static void storeKey(Path keyFile, byte[] encodedKey) throws IOException {
                if (Files.exists(keyFile)) Files.delete(keyFile);
                Files.createFile(keyFile);
                Files.write(keyFile, encodedKey);
            }
        }

        try {
            Helper.storeKey(publicKeyFile, encodedPublicKey);
            Helper.storeKey(privateKeyFile, encodedPrivateKey);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] getEncodedPublicKey() {
        return getEncodedKey("public");
    }

    public static byte[] getEncodedPrivateKey() {
        return getEncodedKey("private");
    }

    private static byte[] getEncodedKey(String name) {
        Path keyFile = keyDir.resolve(name + ".key");
        if (Files.notExists(keyFile)) {throw new IllegalStateException("No key file found: " + keyFile);}

        try {
            return Files.readAllBytes(keyFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<byte[]> getAllEncodedTrustedKeys() {
        ArrayList<byte[]> list = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(trustedKeysDir)) {
            for (Path file : stream) {
                if ("key".equals(FileUtil.getExtension(file))) {
                    byte[] bytes = Files.readAllBytes(file);
                    list.add(bytes);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}
