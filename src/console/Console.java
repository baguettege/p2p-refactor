package console;

import console.inputhandlers.InputHandler;
import console.inputhandlers.MasterInputHandler;
import console.inputhandlers.PeerInputHandler;
import files.FileManager;
import network.Peer;

import javax.swing.*;

public class Console {
    private JTextArea outputArea;
    private InputHandler inputHandler;

    protected Console(JTextField inputField, JTextArea outputArea) { // master console
        setDefaults(inputField, outputArea, new MasterInputHandler());
    }

    protected Console(JTextField inputField, JTextArea outputArea, Peer peer) { // peer console
        setDefaults(inputField, outputArea, new PeerInputHandler(peer));
    }

    private void setDefaults(JTextField inputField, JTextArea outputArea, InputHandler inputHandler) {
        this.outputArea = outputArea;

        inputField.addActionListener(e -> {
            String text = inputField.getText();
            inputField.setText("");

            if (text.isBlank()) return;

            log("> " + text);
            inputHandler.handle(text);
        });

        this.inputHandler = inputHandler;
    }

    public void log(String text) {
        String fmtText = ConsoleUtil.formatLog(text);
        outputArea.append(fmtText + "\n");
        System.out.println(fmtText);
        FileManager.writeToLogFile(fmtText);
    }

    public void close() {
        ConsoleManager.removeConsole(this);
    }
}
