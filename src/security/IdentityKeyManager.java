package security;

import console.ConsoleManager;
import files.FileManager;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;

public class IdentityKeyManager {
    /// handles own long-term identity keys

    private static PublicKey IDPublicKey;
    private static PrivateKey IDPrivateKey;

    public static PublicKey getIDPublicKey() {return IDPublicKey;}
    public static PrivateKey getIDPrivateKey() {return IDPrivateKey;}

    // gets the stored long-term ID keys from the user's files
    // if they do not exist, or only 1 does, then new ones are generated to replace them
    public static void init() {
        byte[] encodedPublicKey;
        byte[] encodedPrivateKey;

        if (!FileManager.identityKeysExist()) {
            KeyPair identityKeyPair = SecurityUtil.generateIdentityKeyPair();

            encodedPublicKey = identityKeyPair.getPublic().getEncoded();
            encodedPrivateKey = identityKeyPair.getPrivate().getEncoded();

            FileManager.setIdentityKeys(encodedPublicKey, encodedPrivateKey);
            ConsoleManager.logMaster("Generated long-term identity keys");

        } else {
            encodedPublicKey = FileManager.getEncodedPublicKey();
            encodedPrivateKey = FileManager.getEncodedPrivateKey();
            ConsoleManager.logMaster("Found long-term identity keys");
        }

        try {
            IDPublicKey = SecurityUtil.convertToPublicKey(encodedPublicKey, "Ed25519");
            IDPrivateKey = SecurityUtil.convertToPrivateKey(encodedPrivateKey, "Ed25519");

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // checks if a given ID public key is in the user's keys/trusted-keys/ directory
    public static boolean isIDKeyTrusted(PublicKey key) {
        byte[] encodedKey = key.getEncoded();
        ArrayList<byte[]> allEncodedTrustedKeys = FileManager.getAllEncodedTrustedKeys();

        for (byte[] encodedTrustedKey : allEncodedTrustedKeys) {
            boolean equal = Arrays.equals(encodedTrustedKey, encodedKey);
            if (equal) return true;
        }

        return false;
    }
}
