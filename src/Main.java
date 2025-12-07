import console.ConsoleManager;
import files.FileManager;
import security.IdentityKeyManager;

public class Main {
    static void main() {
        ConsoleManager.init();
        FileManager.init();
        IdentityKeyManager.init();
    }
}
