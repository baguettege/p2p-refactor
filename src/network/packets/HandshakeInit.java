package network.packets;

import network.PacketDispatcher;
import network.PacketHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.PublicKey;

public class HandshakeInit extends Packet {
    /// used for the host (server-side) peer connection, which is sent to the other peer
    /// holds data for computing DH shared secret
    /// and building a transcript

    private PublicKey DHPublicKey;
    private PublicKey IDPublicKey;
    private BigInteger p;
    private BigInteger g;
    private byte[] nonce;

    protected HandshakeInit() {}
    public HandshakeInit(
            PublicKey DHPublicKey,
            PublicKey IDPublicKey,
            BigInteger p,
            BigInteger g,
            byte[] nonce
    ) {
        this.DHPublicKey = DHPublicKey;
        this.IDPublicKey = IDPublicKey;
        this.p = p;
        this.g = g;
        this.nonce = nonce;
    }

    public PublicKey getDHPublicKey() {return DHPublicKey;}
    public PublicKey getIDPublicKey() {return IDPublicKey;}
    public BigInteger getP() {return p;}
    public BigInteger getG() {return g;}
    public byte[] getNonce() {return nonce;}

    public void dispatch(PacketDispatcher dispatcher) {
        dispatcher.handle(this);
    }

    public void write(DataOutputStream dos) throws IOException {
        PacketHelper.writePublicKey(dos, DHPublicKey);
        PacketHelper.writePublicKey(dos, IDPublicKey);
        PacketHelper.writeBigInteger(dos, p);
        PacketHelper.writeBigInteger(dos, g);
        PacketHelper.writeBytes(dos, nonce);
    }

    public void read(DataInputStream dis) throws IOException {
        DHPublicKey = PacketHelper.readPublicKey(dis, "DH");
        IDPublicKey = PacketHelper.readPublicKey(dis, "Ed25519");
        p = PacketHelper.readBigInteger(dis);
        g = PacketHelper.readBigInteger(dis);
        nonce = PacketHelper.readBytes(dis);
    }
}
