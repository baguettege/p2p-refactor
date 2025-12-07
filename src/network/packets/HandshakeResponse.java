package network.packets;

import network.PacketDispatcher;
import network.PacketHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.PublicKey;

public class HandshakeResponse extends Packet {
    /// used for the non-host (client-side) peer connection, which is sent to the other peer
    /// holds data for computing DH shared secret
    /// and building a transcript

    private PublicKey DHPublicKey;
    private PublicKey IDPublicKey;
    private byte[] nonce;

    protected HandshakeResponse() {}
    public HandshakeResponse(
            PublicKey DHPublicKey,
            PublicKey IDPublicKey,
            byte[] nonce
    ) {
        this.DHPublicKey = DHPublicKey;
        this.IDPublicKey = IDPublicKey;
        this.nonce = nonce;
    }

    public PublicKey getDHPublicKey() {return DHPublicKey;}
    public PublicKey getIDPublicKey() {return IDPublicKey;}
    public byte[] getNonce() {return nonce;}

    public void dispatch(PacketDispatcher dispatcher) {
        dispatcher.handle(this);
    }

    public void write(DataOutputStream dos) throws IOException {
        PacketHelper.writePublicKey(dos, DHPublicKey);
        PacketHelper.writePublicKey(dos, IDPublicKey);
        PacketHelper.writeBytes(dos, nonce);
    }

    public void read(DataInputStream dis) throws IOException {
        DHPublicKey = PacketHelper.readPublicKey(dis, "DH");
        IDPublicKey = PacketHelper.readPublicKey(dis, "Ed25519");
        nonce = PacketHelper.readBytes(dis);
    }
}
