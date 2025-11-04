import java.util.*;
import java.io.*;
import java.util.Base64;
import java.time.*;            // NEW: For working with dates and times
import java.nio.file.*;        // NEW: For advanced file operations
import java.security.*;        // NEW: For future password encryption/hashing

public class PasswordManager {

    // ---------- CLASS: User ----------
    static class User {
        private String username;
        private String masterPassword;

        public User(String username, String masterPassword) {
            this.username = username;
            this.masterPassword = masterPassword;
        }

        public String getUsername() {
            return username;
        }

        public boolean authenticate(String password) {
            return this.masterPassword.equals(password);
        }
    }

    // ---------- CLASS: Credential ----------
    static class Credential {
        private String appName;
        private String username;
        private String password;

        public Credential(String appName, String username, String password) {
            this.appName = appName;
            this.username = username;
            this.password = password;
        }

        public String getAppName() {
            return appName;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        @Override
        public String toString() {
            return "App: " + appName + ", Username: " + username + ", Password: " + password;
        }
    }

    // ---------- CLASS: FileHandler ----------
    static class FileHandler {
        private static final String FILE_NAME = "credentials.txt";

        public static void saveCredentials(List<Credential> credentials) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
                for (Credential c : credentials) {
                    String data = c.getAppName() + "," + c.getUsername() + "," + c.getPassword();
                    String encoded = Base64.getEncoder().encodeToString(data.getBytes());
                    writer.write(encoded);
                    writer.newLine();
                }
            } catch (IOException e) {
                System.out.println("Error saving credentials: " + e.getMessage());
            }
        }

        public static List<Credential> loadCredentials() {
            List<Credential> credentials = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String decoded = new String(Base64.getDecoder().decode(line));
                    String[] parts = decoded.split(",");
                    if (parts.length == 3) {
                        credentials.add(new Credential(parts[0], parts[1], parts[2]));
                    }
                }
            } catch (FileNotFoundException e) {
                // Ignore if file doesn't exist
            } catch (IOException e) {
                System.out.println("Error loading credentials: " + e.getMessage());
            }
            return credentials;
        }
    }

    // ---------- MAIN PASSWORD MANAGER LOGIC ----------
    private User user;
    private List<Credential> credentials;

    public PasswordManager(User user) {
        this.user = user;
        this.credentials = FileHandler.loadCredentials();
    }

    public void addCredential(Scanner sc) {
        System.out.print("Enter app name: ");
        String app = sc.nextLine();
        System.out.print("Enter username: ");
        String uname = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();

        credentials.add(new Credential(app, uname, pass));
        FileHandler.saveCredentials(credentials);
        System.out.println("Credential added successfully!");
    }

    public void viewCredentials() {
        if (credentials.isEmpty()) {
            System.out.println("No credentials found.");
            return;
        }
        for (Credential c : credentials) {
            System.out.println(c);
        }
    }

    public void deleteCredential(Scanner sc) {
        System.out.print("Enter app name to delete: ");
        String app = sc.nextLine();
        boolean removed = credentials.removeIf(c -> c.getAppName().equalsIgnoreCase(app));
        if (removed) {
            FileHandler.saveCredentials(credentials);
            System.out.println("Credential deleted successfully!");
        } else {
            System.out.println("No matching credential found.");
        }
    }

    public void updateCredential(Scanner sc) {
        System.out.print("Enter app name to update: ");
        String app = sc.nextLine();
        for (Credential c : credentials) {
            if (c.getAppName().equalsIgnoreCase(app)) {
                System.out.print("Enter new username: ");
                String uname = sc.nextLine();
                System.out.print("Enter new password: ");
                String pass = sc.nextLine();

                credentials.remove(c);
                credentials.add(new Credential(app, uname, pass));
                FileHandler.saveCredentials(credentials);
                System.out.println("Credential updated successfully!");
                return;
            }
        }
        System.out.println("App not found.");
    }

    // ---------- MAIN METHOD ----------
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== PASSWORD MANAGER ===");

        System.out.print("Register your username: ");
        String uname = sc.nextLine();
        System.out.print("Set master password: ");
        String master = sc.nextLine();

        User user = new User(uname, master);

        System.out.println("\nLogin to continue");
        System.out.print("Username: ");
        String loginUser = sc.nextLine();
        System.out.print("Password: ");
        String loginPass = sc.nextLine();

        if (!user.getUsername().equals(loginUser) || !user.authenticate(loginPass)) {
            System.out.println("Invalid credentials! Exiting...");
            return;
        }

        PasswordManager pm = new PasswordManager(user);

        int choice;
        do {
            System.out.println("\n==== MENU ====");
            System.out.println("1. Add Credential");
            System.out.println("2. View Credentials");
            System.out.println("3. Update Credential");
            System.out.println("4. Delete Credential");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");

            while (!sc.hasNextInt()) {
                System.out.print("Please enter a valid number: ");
                sc.next();
            }
            choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1 -> pm.addCredential(sc);
                case 2 -> pm.viewCredentials();
                case 3 -> pm.updateCredential(sc);
                case 4 -> pm.deleteCredential(sc);
                case 5 -> System.out.println("Goodbye!");
                default -> System.out.println("Invalid choice!");
            }
        } while (choice != 5);
    }
}


