package security;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.*;

public class TranscriptManager {
    protected static byte[] buildHashedTranscript(
            PublicKey yourDHPublicKey,
            PublicKey theirDHPublicKey,
            PublicKey yourIDPublicKey,
            PublicKey theirIDPublicKey,
            byte[] yourNonce,
            byte[] theirNonce
    ) {
        class Helper {
            private static void writeField(DataOutputStream dos, byte[] data) throws IOException {
                dos.writeInt(data.length);
                dos.write(data);
            }
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            Helper.writeField(dos, yourDHPublicKey.getEncoded());
            Helper.writeField(dos, theirDHPublicKey.getEncoded());
            Helper.writeField(dos, yourIDPublicKey.getEncoded());
            Helper.writeField(dos, theirIDPublicKey.getEncoded());
            Helper.writeField(dos, yourNonce);
            Helper.writeField(dos, theirNonce);

            byte[] transcript = baos.toByteArray();

            // hash the transcript
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(transcript);

        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    protected static byte[] signTranscript(
            String algorithm,
            byte[] transcript,
            PrivateKey privateKey
    ) throws NoSuchAlgorithmException, InvalidKeyException {
        Signature sig = Signature.getInstance(algorithm);
        sig.initSign(privateKey);

        try {
            sig.update(transcript);
            return sig.sign();
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    protected static boolean verifyTranscripts(
            String algorithm,
            PublicKey theirIDPublicKey,
            byte[] theirSignature,
            byte[] yourTranscript
    ) throws NoSuchAlgorithmException, InvalidKeyException {
        Signature sig = Signature.getInstance(algorithm);
        sig.initVerify(theirIDPublicKey);

        try {
            sig.update(yourTranscript);
            return sig.verify(theirSignature);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
    }
}
