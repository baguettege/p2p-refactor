package network.packets;

public class PacketFactory {
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
