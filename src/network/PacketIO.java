package network;

import network.packets.Packet;
import network.packets.PacketFactory;
import security.EncryptionManager;

import javax.crypto.spec.SecretKeySpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;

public class PacketIO {
    // writing
    public static void writeEncrypted(DataOutputStream dos, SecretKeySpec secretKeySpec, Packet packet) throws IOException, InvalidKeyException {
        byte[] serializedPacket = PacketSerializer.serialize(packet);
        byte[] cipherText = EncryptionManager.encrypt(serializedPacket, secretKeySpec);
        dos.writeInt(cipherText.length);
        dos.write(cipherText);
    }

    public static void writePlain(DataOutputStream dos, Packet packet) throws IOException {
        dos.writeUTF(packet.getId());
        packet.write(dos);
        dos.flush();
    }

    // reading
    public static Packet readEncrypted(DataInputStream dis, SecretKeySpec secretKeySpec) throws IOException, InvalidKeyException {
        int length = dis.readInt();
        byte[] cipherText = new byte[length];
        dis.readFully(cipherText);

        byte[] plainText = EncryptionManager.decrypt(cipherText, secretKeySpec);

        return PacketSerializer.deserialize(plainText);
    }

    public static Packet readPlain(DataInputStream dis) throws IOException {
        String id = dis.readUTF();
        Packet packet = PacketFactory.create(id);
        if (packet != null) packet.read(dis);
        return packet;
    }
}
