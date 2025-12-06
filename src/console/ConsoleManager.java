package console;

import network.Peer;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ConsoleManager {
    private static JTabbedPane tabs;
    private static final ConcurrentHashMap<Console, JPanel> activeConsolePanels = new ConcurrentHashMap<>();
    private static Console masterConsole;

    public static void init() {
        if (masterConsole != null) return;

        Supplier<Void> supplier = () -> {
            JFrame frame = new JFrame("p2p-refactor");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(700, 500);

            tabs = new JTabbedPane();
            frame.add(tabs);

            frame.setVisible(true);

            masterConsole = createMasterConsole();
            return null;
        };

        // ensure ran on edt
        if (SwingUtilities.isEventDispatchThread()) {
            supplier.get();
            return;
        }

        try {
            SwingUtilities.invokeAndWait(supplier::get);
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void logMaster(String text) {masterConsole.log(text);}

    // console creation ----------
    private static Console createMasterConsole() {
        ConsoleComponents components = createConsoleComponents();
        tabs.add("Master", components.panel);

        Console newConsole = new Console(components.inputField, components.outputArea);
        activeConsolePanels.put(newConsole, components.panel);

        newConsole.log("Console startup | Master");
        return newConsole;
    }

    public static Console createPeerConsole(String name, Peer peer) { // todo: add Peer class param later
        Supplier<Console> supplier = () -> {
            ConsoleComponents components = createConsoleComponents();
            tabs.add(name, components.panel);

            Console newConsole = new Console(components.inputField, components.outputArea, peer);
            activeConsolePanels.put(newConsole, components.panel);

            newConsole.log("Console startup | " + name);
            return newConsole;
        };

        // ensure ran on edt
        if (SwingUtilities.isEventDispatchThread()) return supplier.get();

        final Console[] holder = new Console[1];
        try {
            SwingUtilities.invokeAndWait(() -> holder[0] = supplier.get());
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return holder[0];
    }

    private static ConsoleComponents createConsoleComponents() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextField inputField = new JTextField();
        JTextArea outputArea = new JTextArea();

        JScrollPane scroll = new JScrollPane(outputArea);

        inputField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        inputField.setForeground(Color.WHITE);
        inputField.setBackground(new Color(43, 44, 48));

        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputArea.setForeground(Color.WHITE);
        outputArea.setBackground(new Color(30, 31, 35));
        outputArea.setEditable(false);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(inputField, BorderLayout.SOUTH);

        return new ConsoleComponents(panel, inputField, outputArea);
    }

    private static class ConsoleComponents {
        public JPanel panel;
        public JTextField inputField;
        public JTextArea outputArea;

        public ConsoleComponents(JPanel panel, JTextField inputField, JTextArea outputArea) {
            this.panel = panel;
            this.inputField = inputField;
            this.outputArea = outputArea;
        }
    }

    // console deletion ----------
    protected static void removeConsole(Console console) {
        tabs.remove(activeConsolePanels.get(console));
    }
}
