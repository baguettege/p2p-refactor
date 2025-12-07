package network;

import network.packets.Packet;
import network.packets.PacketFactory;

import java.io.*;

public class PacketSerializer {
    public static byte[] serialize(Packet packet) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeUTF(packet.getId());
        packet.write(dos);

        return baos.toByteArray();
    }

    public static Packet deserialize(byte[] encryptedPacket) throws IOException {
        DataInputStream byteDis = new DataInputStream(new ByteArrayInputStream(encryptedPacket));

        String id = byteDis.readUTF();
        Packet packet = PacketFactory.create(id);

        if (packet != null) packet.read(byteDis);

        return packet;
    }
}
