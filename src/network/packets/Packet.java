package network.packets;

import network.PacketDispatcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Packet {
    /// all packets must extend this class
    /// write() writes all the fields of the packet to the given dos
    /// read() sets all the fields of the packet by reading the given dis

    // dispatches the packet to the peer's packet dispatcher
    public abstract void dispatch(PacketDispatcher dispatcher);

    // given above
    public abstract void write(DataOutputStream dos) throws IOException;
    public abstract void read(DataInputStream dis) throws IOException;

    // used for identifying the type of packet it is, for dispatching and PacketFactory
    public final String getId() {return this.getClass().getSimpleName();}
}
