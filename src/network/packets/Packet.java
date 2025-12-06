package network.packets;

import network.PacketDispatcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Packet {
    public abstract void dispatch(PacketDispatcher dispatcher);

    public abstract void write(DataOutputStream dos) throws IOException;
    public abstract void read(DataInputStream dis) throws IOException;

    public final String getId() {return this.getClass().getSimpleName();}
}
