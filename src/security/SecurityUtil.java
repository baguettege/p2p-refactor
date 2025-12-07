package security;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SecurityUtil {
    /// simply utility for security

    private static final SecureRandom secureRandom = new SecureRandom();

    // generates a nonce with a given length, also used for IV for encryption & decryption
    public static byte[] generateNonce(int length) {
        byte[] nonce = new byte[length];
        secureRandom.nextBytes(nonce);
        return nonce;
    }

    // generates long-term identity key pairs for the user
    public static KeyPair generateIdentityKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
            return kpg.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // converts a byte array to a public key with a given algorithm
    public static PublicKey convertToPublicKey(byte[] encodedPublicKey, String algorithm) throws NoSuchAlgorithmException {
        try {
            KeyFactory kf = KeyFactory.getInstance(algorithm);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedPublicKey);
            return kf.generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    // converts a byte array to a private key with a given algorithm
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
