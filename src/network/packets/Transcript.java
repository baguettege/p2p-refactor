package network.packets;

import network.PacketDispatcher;
import network.PacketHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Transcript extends Packet {
    /// holds the transcript that a peer has built
    /// is verified by the other peer
    /// MUST be hashed AND signed by the peer sending it

    private byte[] transcript;

    protected Transcript() {}
    public Transcript(byte[] transcript) {
        this.transcript = transcript;
    }

    public byte[] getTranscript() {return transcript;}

    public void dispatch(PacketDispatcher dispatcher) {
        dispatcher.handle(this);
    }

    public void write(DataOutputStream dos) throws IOException {
        PacketHelper.writeBytes(dos, transcript);
    }

    public void read(DataInputStream dis) throws IOException {
        transcript = PacketHelper.readBytes(dis);
    }
}
