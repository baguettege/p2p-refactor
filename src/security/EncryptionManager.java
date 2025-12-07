package security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class EncryptionManager {
    public static byte[] encrypt(byte[] plainText, SecretKeySpec secretKeySpec) throws InvalidKeyException {
        byte[] iv = SecurityUtil.generateNonce(12);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpec);
            byte[] cipherText = cipher.doFinal(plainText);

            baos.write(iv);
            baos.write(cipherText);

            return baos.toByteArray();

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
                 IllegalBlockSizeException | BadPaddingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decrypt(byte[] cipherText, SecretKeySpec secretKeySpec) throws InvalidKeyException {
        if (cipherText.length < 12) throw new IllegalArgumentException("Cipher text too short; unable to decrypt");

        byte[] iv = Arrays.copyOfRange(cipherText, 0, 12);
        byte[] realCipherText = Arrays.copyOfRange(cipherText, 12, cipherText.length);

        try {
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmSpec);
            return cipher.doFinal(realCipherText);

        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                 IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}
