package network;

import network.packets.*;

public class PacketDispatcher {
    /// takes a packet sent by a peer and dispatches it to the corresponding method so that it can be dealt with

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

    public void handle(HandshakeInit packet) {
        peer.getSecurityManager().takeHandshakeInit(
                packet.getDHPublicKey(),
                packet.getIDPublicKey(),
                packet.getP(),
                packet.getG(),
                packet.getNonce()
        );
    }

    public void handle(HandshakeResponse packet) {
        peer.getSecurityManager().takeHandshakeResponse(
                packet.getDHPublicKey(),
                packet.getIDPublicKey(),
                packet.getNonce()
        );
    }

    public void handle(Transcript packet) {
        peer.getSecurityManager().takeTranscript(packet.getTranscript());
    }
}
