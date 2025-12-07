package security;

import network.Peer;
import network.packets.HandshakeInit;
import network.packets.HandshakeResponse;
import network.packets.Transcript;

import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class SecurityManager {
    private final Peer peer;
    private final boolean isHost;

    private KeyPair DHKeyPair;
    private final byte[] nonce = SecurityUtil.generateNonce(32);

    private SecretKeySpec aesKey;

    private PublicKey peerIDPublicKey;
    private byte[] myHashedTranscript;

    public boolean canEncrypt() {return aesKey != null;}
    public SecretKeySpec getAesKey() {return aesKey;}

    public SecurityManager(Peer peer, boolean isHost) {
        this.peer = peer;
        this.isHost = isHost;
    }

    public void init() { // host side
        if (!isHost) return;

        DHKeyPair = DHManager.generateKeyPair(4096);
        peer.logConsole("Generated DH key pair with keysize 4096");

        DHManager.DHParameters parameters = DHManager.extractDHParameters(DHKeyPair.getPublic());
        BigInteger p = parameters.p;
        BigInteger g = parameters.g;

        peer.write(new HandshakeInit(
                DHKeyPair.getPublic(),
                IdentityKeyManager.getIDPublicKey(),
                p,
                g,
                nonce
        ));
    }

    private void init(BigInteger p, BigInteger g) { // non host side
        if (isHost) return;

        DHKeyPair = DHManager.generateKeyPair(p, g);
        peer.logConsole("Generated DH key pair from given prime and generator");

        peer.write(new HandshakeResponse(
                DHKeyPair.getPublic(),
                IdentityKeyManager.getIDPublicKey(),
                nonce
        ));
    }

    public void takeHandshakeResponse( // host side
                                       PublicKey DHPublicKey,
                                       PublicKey IDPublicKey,
                                       byte[] nonce
    ) {
        peerIDPublicKey = IDPublicKey;
        if (!IdentityKeyManager.isIDKeyTrusted(IDPublicKey)) {
            peer.disconnect("Long-term public identity key is untrusted");
            return;
        } else {
            peer.logConsole("Long-term public identity key is trusted");
        }

        aesKey = DHManager.computeSharedSecret(DHKeyPair.getPrivate(), DHPublicKey);
        peer.logConsole("Computed AES key");

        myHashedTranscript = TranscriptManager.buildHashedTranscript(
                DHKeyPair.getPublic(),
                DHPublicKey,
                IdentityKeyManager.getIDPublicKey(),
                IDPublicKey,
                this.nonce,
                nonce
        );

        try {
            byte[] signature = TranscriptManager.signTranscript("Ed25519", myHashedTranscript, IdentityKeyManager.getIDPrivateKey());
            peer.write(new Transcript(signature));

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public void takeHandshakeInit( // non host side
            PublicKey DHPublicKey,
            PublicKey IDPublicKey,
            BigInteger p,
            BigInteger g,
            byte[] nonce
    ) {
        peerIDPublicKey = IDPublicKey;
        if (!IdentityKeyManager.isIDKeyTrusted(IDPublicKey)) {
            peer.disconnect("Long-term public identity key is untrusted");
            return;
        } else {
            peer.logConsole("Long-term public identity key is trusted");
        }

        init(p, g);
        aesKey = DHManager.computeSharedSecret(DHKeyPair.getPrivate(), DHPublicKey);
        peer.logConsole("Computed AES key");

        myHashedTranscript = TranscriptManager.buildHashedTranscript(
                DHPublicKey,
                DHKeyPair.getPublic(),
                IDPublicKey,
                IdentityKeyManager.getIDPublicKey(),
                nonce,
                this.nonce
        );

        try {
            byte[] signature = TranscriptManager.signTranscript("Ed25519", myHashedTranscript, IdentityKeyManager.getIDPrivateKey());
            peer.write(new Transcript(signature));

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public void takeTranscript(byte[] signature) {
        try {
            boolean verified = TranscriptManager.verifyTranscripts(
                    "Ed25519",
                    peerIDPublicKey,
                    signature,
                    myHashedTranscript
            );

            if (verified) {
                peer.logConsole("Transcript verified");
            } else {
                peer.disconnect("Transcript was not verified - MITM possible");
            }

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        peer.logConsole("""
                Encryption info:
                - Cipher: AES/GCM/NoPadding
                - Key derivation: SHA-256
                - Key exchange: FFDHE4096
                - ID key algorithm: Ed25519
                """);
    }
}
