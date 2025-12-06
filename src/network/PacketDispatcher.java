package network;

import network.packets.Message;
import network.packets.Packet;

public class PacketDispatcher {
    private final Peer peer;

    protected PacketDispatcher(Peer peer) {
        this.peer = peer;
    }

    protected void dispatch(Packet packet) {
        packet.dispatch(this);
    }

    // packet handlers

    public void handle(Message packet) {
        peer.logConsole("MSG | " + packet.getText());
    }
}
