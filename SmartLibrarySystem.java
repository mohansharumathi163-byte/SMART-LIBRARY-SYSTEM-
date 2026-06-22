import java.util.*;
import java.util.concurrent.*;

public class SmartLibrarySystem {

    static class Room {
        String name;
        boolean booked;
        double temperature;
        double light;
        String statusMessage;
        ScheduledFuture<?> autoReleaseTask;

        Room(String name) {
            this.name = name;
            this.booked = false;
            simulateEnvironment();
            this.statusMessage = "Power saving mode active";
        }

        void simulateEnvironment() {
            temperature = 20 + Math.random() * 10; // 20-30°C
            light = 50 + Math.random() * 30;       // 50-80%
            updateStatusMessage();
        }

        void updateStatusMessage() {
            if (booked) {
                if (temperature > 27) statusMessage = "High temp – AC ON";
                else if (light < 55) statusMessage = "Low light – Increasing brightness";
                else statusMessage = "Room occupied – Optimal environment";
            } else {
                statusMessage = "Power saving mode active";
            }
        }
    }

    static class Customer {
        String name;
        int studyStreak;
        List<String> badges;

        Customer(String name) {
            this.name = name;
            this.studyStreak = 0;
            this.badges = new ArrayList<>();
        }

        void completeSession() {
            studyStreak++;
            if (studyStreak % 5 == 0) badges.add("🏅 " + studyStreak + " Sessions Badge");
        }
    }

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "1234";
    private static boolean running = true;
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Map<String, Room> rooms = new LinkedHashMap<>();
        Map<String, Customer> customers = new HashMap<>();

        for (int i = 1; i <= 5; i++) rooms.put("Study Room " + i, new Room("Study Room " + i));

        System.out.println("===== SMART LIBRARY SYSTEM =====");

        System.out.print("Login as (admin/user): ");
        String role = sc.nextLine().trim().toLowerCase();

        if (role.equals("admin")) {
            if (!loginAdmin(sc)) return;
            adminMenu(sc, rooms);
        } else {
            System.out.print("Enter your name: ");
            String cname = sc.nextLine().trim();
            Customer customer = customers.computeIfAbsent(cname, Customer::new);
            System.out.println("\nLogged in as Customer.");
            customerMenu(sc, rooms, customer);
        }

        scheduler.shutdown();
        System.out.println("System shutting down...");
    }

    // ---------------- ADMIN ----------------
    private static boolean loginAdmin(Scanner sc) {
        for (int attempts = 0; attempts < 3; attempts++) {
            System.out.print("Username: ");
            String u = sc.nextLine();
            System.out.print("Password: ");
            String p = sc.nextLine();
            if (u.equals(ADMIN_USERNAME) && p.equals(ADMIN_PASSWORD)) {
                System.out.println("\nLogin successful! Welcome, Admin.");
                return true;
            } else {
                System.out.println("Invalid credentials. Try again.");
            }
        }
        System.out.println("Too many failed attempts. Exiting...");
        return false;
    }

    private static void adminMenu(Scanner sc, Map<String, Room> rooms) {
        int choice;
        do {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. View All Rooms");
            System.out.println("2. Book Room");
            System.out.println("3. Free Room");
            System.out.println("4. Dashboard Summary");
            System.out.println("5. Add Room");
            System.out.println("6. Remove Room");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            choice = getInt(sc);

            switch (choice) {
                case 1 -> viewRooms(rooms);
                case 2 -> bookRoom(rooms, sc);
                case 3 -> freeRoom(rooms, sc);
                case 4 -> dashboard(rooms);
                case 5 -> addRoom(rooms, sc);
                case 6 -> removeRoom(rooms, sc);
                case 0 -> running = false;
                default -> System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }

    // ---------------- CUSTOMER ----------------
    private static void customerMenu(Scanner sc, Map<String, Room> rooms, Customer customer) {
        int choice;
        do {
            System.out.println("\n===== CUSTOMER MENU =====");
            System.out.println("1. View Available Rooms");
            System.out.println("2. Book Room");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            choice = getInt(sc);

            switch (choice) {
                case 1 -> viewAvailableRooms(rooms);
                case 2 -> bookRoomCustomer(rooms, sc, customer);
                case 0 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }

    // ---------------- ROOM OPERATIONS ----------------
    private static void viewRooms(Map<String, Room> rooms) {
        System.out.println("\n---- ROOM STATUS ----");
        for (Room r : rooms.values()) {
            System.out.printf("%-15s | Temp: %.1f°C | Light: %.0f%% | %s%n",
                    r.name, r.temperature, r.light, r.statusMessage);
        }
    }

    private static void viewAvailableRooms(Map<String, Room> rooms) {
        System.out.println("\n---- AVAILABLE ROOMS ----");
        for (Room r : rooms.values()) {
            if (!r.booked) {
                System.out.printf("%-15s | Temp: %.1f°C | Light: %.0f%% | [Idle] %s%n",
                        r.name, r.temperature, r.light, r.statusMessage);
            }
        }
    }

    private static void bookRoom(Map<String, Room> rooms, Scanner sc) {
        System.out.print("Enter room name to book: ");
        String name = sc.nextLine();
        Room r = rooms.get(name);
        if (r == null) System.out.println("Room not found!");
        else if (r.booked) System.out.println("Room is already booked!");
        else {
            r.booked = true;
            r.updateStatusMessage();
            System.out.println(r.name + " booked successfully!");
        }
    }

    private static void freeRoom(Map<String, Room> rooms, Scanner sc) {
        System.out.print("Enter room name to free: ");
        String name = sc.nextLine();
        Room r = rooms.get(name);
        if (r == null) System.out.println("Room not found!");
        else if (!r.booked) System.out.println("Room is already free!");
        else {
            r.booked = false;
            r.updateStatusMessage();
            System.out.println(r.name + " is now free!");
        }
    }

    private static void bookRoomCustomer(Map<String, Room> rooms, Scanner sc, Customer customer) {
        System.out.print("Enter room name to book: ");
        String name = sc.nextLine();
        Room r = rooms.get(name);
        if (r == null) System.out.println("Room not found!");
        else if (r.booked) System.out.println("Room is already booked!");
        else {
            System.out.print("Enter booking duration (minutes): ");
            int duration = getInt(sc);
            r.booked = true;
            r.updateStatusMessage();
            System.out.println(r.name + " booked for " + duration + " minutes.");

            // Auto-release task
            r.autoReleaseTask = scheduler.schedule(() -> {
                r.booked = false;
                r.updateStatusMessage();
                System.out.println("\nAuto-release: " + r.name + " is now free!");
            }, duration, TimeUnit.MINUTES);

            // Increment user study streak
            customer.completeSession();
            System.out.println("✅ " + customer.name + " study streak: " + customer.studyStreak);
            if (!customer.badges.isEmpty())
                System.out.println("Badges: " + String.join(", ", customer.badges));
        }
    }

    private static void addRoom(Map<String, Room> rooms, Scanner sc) {
        System.out.print("Enter new room name: ");
        String name = sc.nextLine();
        if (rooms.containsKey(name)) System.out.println("Room already exists!");
        else {
            rooms.put(name, new Room(name));
            System.out.println(name + " added successfully!");
        }
    }

    private static void removeRoom(Map<String, Room> rooms, Scanner sc) {
        System.out.print("Enter room name to remove: ");
        String name = sc.nextLine();
        if (!rooms.containsKey(name)) System.out.println("Room not found!");
        else {
            rooms.remove(name);
            System.out.println(name + " removed successfully!");
        }
    }

    // ---------------- DASHBOARD ----------------
    private static void dashboard(Map<String, Room> rooms) {
        int total = rooms.size();
        long booked = rooms.values().stream().filter(r -> r.booked).count();
        long free = total - booked;
        double avgTemp = rooms.values().stream().mapToDouble(r -> r.temperature).average().orElse(0);
        double avgLight = rooms.values().stream().mapToDouble(r -> r.light).average().orElse(0);

        System.out.println("\n===== DASHBOARD SUMMARY =====");
        System.out.println("Total Rooms: " + total);
        System.out.println("Booked Rooms: " + booked);
        System.out.println("Free Rooms: " + free);
        System.out.printf("Average Temperature: %.1f°C%n", avgTemp);
        System.out.printf("Average Light Level: %.0f%%%n", avgLight);
    }

    // ---------------- HELPERS ----------------
    private static int getInt(Scanner sc) {
        try { return Integer.parseInt(sc.nextLine()); }
        catch (Exception e) { return -1; }
    }
}