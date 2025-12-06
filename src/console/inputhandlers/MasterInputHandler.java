package console.inputhandlers;

import console.ConsoleManager;
import network.NetworkManager;

public class MasterInputHandler implements InputHandler {
    private void invalidInput(String reason) {
        ConsoleManager.logMaster("Invalid cmd: " + reason);
    }

    public void handle(String text) {
        if (text.isBlank()) return;

        String[] args = text.split(" ");

        switch (args[0]) {
            case "cmd" -> printCmds();
            case "conn" -> handleConn(args);
            case "listen" -> handleListen(args);
            default -> invalidInput("Unknown cmd: " + args[0]);
        }
    }

    private void printCmds() {
        ConsoleManager.logMaster("""
                Available cmds:
                - cmd
                - conn [ip] [port]
                - listen [port]
                - listen stop
                """);
    }

    private void handleConn(String[] args) {
        if (args.length != 3) {invalidInput("Expected 3 args"); return;}

        String ip = args[1];
        int port;
        try {
            port = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            invalidInput("Entered port was not a number");
            return;
        }

        if (port < 0 || port > 65535) {
            invalidInput("Port entered was not between 0-65535");
            return;
        }

        NetworkManager.connect(ip, port);
    }

    private void handleListen(String[] args) {
        if (args.length != 2) {invalidInput("Expected 2 args"); return;}

        try {
            int port = Integer.parseInt(args[1]);

            if (port < 0 || port > 65535) {
                invalidInput("Port entered was not between 0-65535");
                return;
            }

            NetworkManager.startListening(port);

        } catch (NumberFormatException _) { // nan, so is 'close' or invalid
            if ("stop".equals(args[1])) {
                NetworkManager.stopListening();
            } else {
                invalidInput("Invalid arg: " + args[1]);
            }
        }
    }
}
