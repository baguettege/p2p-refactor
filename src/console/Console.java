package console;

import console.inputhandlers.InputHandler;
import console.inputhandlers.MasterInputHandler;
import console.inputhandlers.PeerInputHandler;
import files.FileManager;
import network.Peer;

import javax.swing.*;

public class Console {
    /// simply a console
    /// holds a JTextArea for outputting logs
    /// holds a JTextField for user input, then passes it onto the given InputHandler
    /// always logs to the sessions log file in FileManager

    private JTextArea outputArea;

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
