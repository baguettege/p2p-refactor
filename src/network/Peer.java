package network;

import console.Console;
import console.ConsoleManager;
import network.packets.Packet;
import network.packets.PacketFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Peer implements Runnable {
    private final Socket socket;
    private final DataOutputStream dos;
    private final DataInputStream dis;

    private final Console console;
    private final PacketDispatcher dispatcher = new PacketDispatcher(this);

    protected Peer(Socket socket) {
        console = ConsoleManager.createPeerConsole(socket.getRemoteSocketAddress().toString(), this);
        this.socket = socket;

        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void write(Packet packet) {
        try {
            dos.writeUTF(packet.getId());
            packet.write(dos);
            dos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() { // listen for packets
        while (true) {
            try {
                String id = dis.readUTF();
                Packet newPacket = PacketFactory.create(id);

                if (newPacket == null) {
                    disconnect("Unknown packet id received: " + id);
                    return;
                }

                newPacket.read(dis);
                dispatcher.dispatch(newPacket);

            } catch (IOException e) {
                disconnect(e.toString());
                return;
            }
        }
    }

    private boolean isDisconnected = false;
    protected synchronized void disconnect(String reason) {
        if (isDisconnected) return;
        isDisconnected = true;

        String ip = "Unknown ip";
        if (socket != null) ip = socket.getRemoteSocketAddress().toString();

        try {
            if (dos != null) dos.close();
            if (dis != null) dis.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ConsoleManager.logMaster("Disconnected from peer: " + ip + "\nReason: " + reason);
        NetworkManager.removeActivePeer(this);
        console.close();
    }

    public void logConsole(String text) {
        console.log(text);
    }
}
