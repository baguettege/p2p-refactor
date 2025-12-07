package security;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.*;

public class DHManager {
    /// provies utility methods for diffie-hellman

    // generates DH public & private keys by generating a prime and generator
    protected static KeyPair generateKeyPair(int keysize) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
            kpg.initialize(keysize);
            return kpg.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // generates DH public & private keys by using a given prime and generator
    protected static KeyPair generateKeyPair(BigInteger p, BigInteger g) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
            kpg.initialize(new DHParameterSpec(p, g));
            return kpg.generateKeyPair();

        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    // computes a shared secret key (AES, derived from SHA-256) from own DH private key and peer's DH public key
    protected static SecretKeySpec computeSharedSecret(PrivateKey privateKey, PublicKey publicKey) {
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);
            byte[] sharedSecretBytes = keyAgreement.generateSecret();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] aesKey = digest.digest(sharedSecretBytes);

            return new SecretKeySpec(aesKey, "AES");

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    // extracts prime and generator from a DH public key
    protected static DHParameters extractDHParameters(PublicKey publicKey) {
        DHPublicKey dhPublicKey = (DHPublicKey) publicKey;
        DHParameterSpec parameterSpec = dhPublicKey.getParams();
        return new DHParameters(parameterSpec.getP(), parameterSpec.getG());
    }

    protected static class DHParameters {
        public BigInteger p;
        public BigInteger g;

        private DHParameters(BigInteger p, BigInteger g) {
            this.p = p;
            this.g = g;
        }
    }
}
