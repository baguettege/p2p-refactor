package network.packets;

import network.PacketDispatcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Message extends Packet {
    private String text;

    protected Message() {}
    public Message(String text) {
        this.text = text;
    }

    public String getText() {return text;}

    @Override
    public void dispatch(PacketDispatcher dispatcher) {
        dispatcher.handle(this);
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeUTF(text);
    }

    public void read(DataInputStream dis) throws IOException {
        text = dis.readUTF();
    }
}
