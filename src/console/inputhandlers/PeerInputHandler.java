package console.inputhandlers;

import network.Peer;
import network.packets.Message;

import java.util.Arrays;

public class PeerInputHandler implements InputHandler {
    /// input handler for the Peer console
    /// one is used for every connection to a peer
    /// takes a command in handle(String text), finds the corresponding command and executes it
    /// will output an error if the input is invalid

    private final Peer peer;

    private void invalidInput(String reason) {
        peer.logConsole("Invalid cmd: " + reason);
    }

    public PeerInputHandler(Peer peer) {
        this.peer = peer;
    }

    public void handle(String text) {
        if (text.isBlank()) return;

        String[] args = text.split(" ");

        switch (args[0]) {
            case "cmd" -> printCmds();
            case "msg" -> handleMsg(args);
            default -> invalidInput("Unknown cmd: " + args[0]);
        }
    }

    private void printCmds() {
        peer.logConsole("""
                Available cmds:
                - cmd
                - msg [text]
                """);
    }

    private void handleMsg(String[] args) {
        if (args.length < 2) {invalidInput("Expected message text"); return;}

        String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        peer.logConsole("MSG (You) | " + text);
        peer.write(new Message(text));
    }
}
