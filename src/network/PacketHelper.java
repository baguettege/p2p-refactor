package network;

import security.SecurityUtil;

import java.io.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class PacketHelper {
    // writing
    public static void writePublicKey(DataOutputStream dos, PublicKey publicKey) throws IOException {
        byte[] bytes = publicKey.getEncoded();
        dos.writeInt(bytes.length);
        dos.write(bytes);
    }

    public static void writeBigInteger(DataOutputStream dos, BigInteger bigInteger) throws IOException {
        byte[] bytes = bigInteger.toByteArray();
        dos.writeInt(bytes.length);
        dos.write(bytes);
    }

    public static void writeBytes(DataOutputStream dos, byte[] bytes) throws IOException {
        dos.writeInt(bytes.length);
        dos.write(bytes);
    }

    // reading
    public static PublicKey readPublicKey(DataInputStream dis, String algorithm) throws IOException {
        int length = dis.readInt();
        byte[] keyBytes = new byte[length];
        dis.readFully(keyBytes);
        try {
            return SecurityUtil.convertToPublicKey(keyBytes, algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static BigInteger readBigInteger(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        byte[] bytes = new byte[length];
        dis.readFully(bytes);
        return new BigInteger(bytes);
    }

    public static byte[] readBytes(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        byte[] bytes = new byte[length];
        dis.readFully(bytes);
        return bytes;
    }
}
