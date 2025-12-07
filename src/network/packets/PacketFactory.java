package network.packets;

public class PacketFactory {
    /// creates empty packets when deserializing packets
    /// every new packet made in src.network.packets must be added to the switch case here

    public static Packet create(String id) {
        return switch (id) {
            case "Message" -> new Message();
            case "HandshakeInit" -> new HandshakeInit();
            case "HandshakeResponse" -> new HandshakeResponse();
            case "Transcript" -> new Transcript();
            default -> null;
        };
    }
}
