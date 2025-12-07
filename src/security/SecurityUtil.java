package security;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SecurityUtil {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static byte[] generateNonce(int length) {
        byte[] nonce = new byte[length];
        secureRandom.nextBytes(nonce);
        return nonce;
    }

    public static KeyPair generateIdentityKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
            return kpg.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey convertToPublicKey(byte[] encodedPublicKey, String algorithm) throws NoSuchAlgorithmException {
        try {
            KeyFactory kf = KeyFactory.getInstance(algorithm);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedPublicKey);
            return kf.generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static PrivateKey convertToPrivateKey(byte[] encodedPrivateKey, String algorithm) throws NoSuchAlgorithmException {
        try {
            KeyFactory kf = KeyFactory.getInstance(algorithm);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
            return kf.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
