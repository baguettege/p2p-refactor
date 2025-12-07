package network;

import console.Console;
import console.ConsoleManager;
import network.packets.Packet;
import security.SecurityManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;

public class Peer implements Runnable {
    private final Socket socket;
    private final DataOutputStream dos;
    private final DataInputStream dis;

    private final Console console;
    private final PacketDispatcher dispatcher = new PacketDispatcher(this);
    private final SecurityManager securityManager;

    protected Peer(Socket socket, boolean isHost) {
        console = ConsoleManager.createPeerConsole(socket.getRemoteSocketAddress().toString(), this);
        this.socket = socket;

        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        securityManager = new SecurityManager(this, isHost);
        if (isHost) securityManager.init();
    }

    public SecurityManager getSecurityManager() {return securityManager;}

    public synchronized void write(Packet packet) {
        try {
            if (securityManager.canEncrypt()) {
                try {
                    PacketIO.writeEncrypted(dos, securityManager.getAesKey(), packet);
                } catch (InvalidKeyException e) {
                    throw new RuntimeException(e);
                }

            } else {
                String id = packet.getId();

                if (!"HandshakeInit".equals(id) && !"HandshakeResponse".equals(id)) {
                    console.log("Attempted to write unencrypted packet: " + id);
                } else {
                    PacketIO.writePlain(dos, packet);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() { // listen for packets

        // wait for handshake timeout
        new Thread(() -> {
            try {
                Thread.sleep(securityManager.getHandshakeTimeout());
                if (!securityManager.isHandshakeCompleted()) {
                    disconnect("Handshake timeout");
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        while (true) {
            try {
                Packet packet;
                if (securityManager.canEncrypt()) {
                    try {
                        packet = PacketIO.readEncrypted(dis, securityManager.getAesKey());
                    } catch (InvalidKeyException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    packet = PacketIO.readPlain(dis);
                }

                dispatcher.dispatch(packet);

            } catch (IOException e) {
                disconnect("Connection was closed by the remote host: " + e);
                return;
            }
        }
    }

    private boolean isDisconnected = false;
    public synchronized void disconnect(String reason) {
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
