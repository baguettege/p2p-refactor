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
    /// this object is made every time a connection to a peer is made over a socket
    /// holds necessary info like data streams, sockets, console

    private final Socket socket;
    private final DataOutputStream dos;
    private final DataInputStream dis;

    private final Console console;
    private final PacketDispatcher dispatcher = new PacketDispatcher(this);
    private final SecurityManager securityManager;

    // isHost is true when the connection was inbound (i.e. server-side)
    protected Peer(Socket socket, boolean isHost) {
        console = ConsoleManager.createPeerConsole(socket.getRemoteSocketAddress().toString(), this);
        this.socket = socket;

        try {
            socket.setKeepAlive(true);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        securityManager = new SecurityManager(this, isHost);
        if (isHost) securityManager.init();
    }

    public SecurityManager getSecurityManager() {return securityManager;}

    // writes a packet to the data output stream
    // will encrypt all packets
    // only allows plain text HandshakeInit and HandshakeResponse packets to be written, as these are safe over an unsafe network
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

    // listens for packets sent by the peer, and decrypts them if an aes key was computed
    // begins a countdown for the handshake, if not completed then disconnects (either took too long or not a real person)
    @Override
    public void run() {
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

    // closes the socket to the peer and closes the console
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
