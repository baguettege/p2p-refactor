package network;

import console.ConsoleManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkManager {
    /// manages the server socket that listens for inbound connections from peers
    /// and outbound connections to other peers
    /// once a connection is made, a Peer object is made

    private static final CopyOnWriteArrayList<Peer> activePeers = new CopyOnWriteArrayList<>();

    /// inbound connections
    // opens a server socket that listens for inbound connections, once one is made, a Peer object is created
    private static ServerSocket serverSocket;
    public static void startListening(int port) {
        if (serverSocket != null && !serverSocket.isClosed()) {
            ConsoleManager.logMaster("Already listening for inbound connections on port " + serverSocket.getLocalPort());
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
            ConsoleManager.logMaster("Listening for inbound connections on port " + serverSocket.getLocalPort());

            new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ConsoleManager.logMaster("Connection received: " + clientSocket.getRemoteSocketAddress());
                        Peer newPeer = new Peer(clientSocket, true);
                        activePeers.add(newPeer);
                        new Thread(newPeer).start();

                    } catch (SocketException _) {
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        ConsoleManager.logMaster("Error whilst listening for connections: " + e);
                    }
                }
            }).start();

        } catch (IOException e) {
            ConsoleManager.logMaster("Failed to open server socket: " + e);
        }
    }

    // closes the server socket that was opened
    public static void stopListening() {
        if (serverSocket == null || serverSocket.isClosed()) {
            ConsoleManager.logMaster("Already not listening for inbound connections");
            return;
        }

        try {
            serverSocket.close();
            serverSocket = null;
            ConsoleManager.logMaster("Stopped listening for inbound connections");

        } catch (IOException e) {
            ConsoleManager.logMaster("Failed to close server socket\nReason: " + e);
        }
    }

    /// outbound connections
    // attempts to connect to a peer, if successfully, Peer object is made
    public static void connect(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            Peer peer = new Peer(socket, false);
            ConsoleManager.logMaster("Connected to: " + socket.getRemoteSocketAddress());
            activePeers.add(peer);
            new Thread(peer).start();

        } catch (IOException e) {
            ConsoleManager.logMaster("Failed to connect\nReason: " + e);
        }
    }

    // both
    protected static void removeActivePeer(Peer peer) {
        activePeers.remove(peer);
    }
}
