package network;

import network.packets.Packet;
import security.EncryptionManager;

import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class PacketIO {

    /// writing ------------------------------------------

    // takes a packet, compresses it if it can be, then encrypts it & writes it to the given dos
    public static void writeEncrypted(DataOutputStream dos, SecretKeySpec secretKeySpec, Packet packet) throws IOException, InvalidKeyException {
        byte[] serializedPacket = PacketSerializer.serialize(packet);

        boolean canZip = canZip(serializedPacket);
        if (canZip) serializedPacket = compress(serializedPacket);

        serializedPacket = EncryptionManager.encrypt(serializedPacket, secretKeySpec);
        byte[] finalData = prependZipBoolean(serializedPacket, canZip);

        PacketHelper.writeBytes(dos, finalData);
    }

    // takes a packet, compresses it if it can be & writes it to the given dos
    public static void writePlain(DataOutputStream dos, Packet packet) throws IOException {
        byte[] serializedPacket = PacketSerializer.serialize(packet);

        boolean canZip = canZip(serializedPacket);
        if (canZip) serializedPacket = compress(serializedPacket);

        byte[] finalData = prependZipBoolean(serializedPacket, canZip);
        PacketHelper.writeBytes(dos, finalData);
    }

    /// reading ------------------------------------------

    // reads data from the given dis, extracts the (encrypted) serialized packet, decrypts it & decompresses if it needs to be
    public static Packet readEncrypted(DataInputStream dis, SecretKeySpec secretKeySpec) throws IOException, InvalidKeyException {
        byte[] cipherText = PacketHelper.readBytes(dis);
        byte[] encryptedPacket = extractSerializedPacket(cipherText);
        byte[] serializedPacket = EncryptionManager.decrypt(encryptedPacket, secretKeySpec);
        return createPacket(serializedPacket, isZipped(cipherText));
    }

    // reads data from the given dis, extracts the (non-encrypted) serialized packet & decompresses if it needs to be
    public static Packet readPlain(DataInputStream dis) throws IOException {
        byte[] data = PacketHelper.readBytes(dis);
        byte[] serializedPacket = extractSerializedPacket(data);
        return createPacket(serializedPacket, isZipped(data));
    }

    // create a packet from its serialized bytes & decompresses it if its zipped
    private static Packet createPacket(byte[] serializedPacket, boolean isZipped) throws IOException {
        if (isZipped) serializedPacket = decompress(serializedPacket);
        return PacketSerializer.deserialize(serializedPacket);
    }

    // sent data is in the form [boolean isZipped][byte[] serializedPacket], extract into just the data
    private static byte[] extractSerializedPacket(byte[] data) {
        return Arrays.copyOfRange(data, 1, data.length);
    }

    /// compression ------------------------------------------

    // compresses a byte array using zis
    public static byte[] compress(byte[] uncompressed) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("compressed-packet"));
            zos.write(uncompressed);
        }

        return baos.toByteArray();
    }

    // decompresses a byte array using zos
    public static byte[] decompress(byte[] compressed) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipInputStream zis = new ZipInputStream(bais)) {
            ZipEntry ze = zis.getNextEntry();
            if (ze != null) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
            }
        }

        return baos.toByteArray();
    }

    // returns true if data size is greater than 8KiB
    private static boolean canZip(byte[] data) {
        long ZIP_THRESHOLD = 8 * 1024; // 8KiB
        return data.length > ZIP_THRESHOLD;
    }

    // sent data is in the form [boolean isZipped][byte[] serializedPacket], returns the boolean
    private static boolean isZipped(byte[] data) {
        return data[0] == 1;
    }

    // appends [boolean isZipped] to [byte[] serializedPacket]
    private static byte[] prependZipBoolean(byte[] data, boolean isZipped) { // sets byte[] to [isZipped][data]
        byte[] newArr = new byte[data.length + 1];
        byte zip = (byte) (isZipped ? 1 : 0);
        newArr[0] = zip;
        System.arraycopy(data, 0, newArr, 1, data.length);
        return newArr;
    }
}
