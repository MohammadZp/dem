package ir.dotin.exam.corejava;

import java.util.*;

public class SimpleOptionalExample {

    static class User {
        String name;
        int age;

        User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        String getName() { return name; }
        int getAge() { return age; }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return age == user.age && Objects.equals(name, user.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }

    // ========== WITHOUT OPTIONAL (Old Way) ==========
    public static User findUserOld(List<User> users, String name) {
        for (User u : users) {
            if (u.getName().equals(name)) {
                return u;
            }
        }
        return null;  // ⚠️ Returns null if not found
    }

    // ========== WITH OPTIONAL (New Way) ==========
    public static Optional<User> findUserNew(List<User> users, String name) {
        for (User u : users) {
            if (u.getName().equals(name)) {
                return Optional.of(u);
            }
        }
        return Optional.empty();  // ✅ Explicitly says "maybe no value"
    }

    public static void main(String[] args) {
        List<User> users = Arrays.asList(
                new User("Alice", 25),
                new User("Bob", 30),
                new User("Charlie", 35)
        );

        // ========== USING THE OLD WAY (DANGEROUS) ==========
        System.out.println("=== OLD WAY (returns null) ===");

        User found1 = findUserOld(users, "Bob");
        System.out.println(found1.getName());  // Works: "Bob"

        User found2 = findUserOld(users, "David");
        // System.out.println(found2.getName());  // 💥 CRASHES! NullPointerException!

        // You have to remember to check for null:
        User safeFind = findUserOld(users, "David");
        if (safeFind != null) {  // Easy to forget!
            System.out.println(safeFind.getName());
        } else {
            System.out.println("User not found");
        }

        // ========== USING THE NEW WAY (SAFE) ==========
        System.out.println("\n=== NEW WAY (returns Optional) ===");

        Optional<User> found3 = findUserNew(users, "Bob");
        found3.ifPresent(u -> System.out.println(u.getName()));  // Works: "Bob"

        Optional<User> found4 = findUserNew(users, "David");

        // Option 1: Provide default behavior
        String name = found4.map(User::getName).orElse("Unknown");
        System.out.println("Name: " + name);  // "Unknown"

        // Option 2: Only act if found
        found4.ifPresent(u -> System.out.println(u.getName()));  // Nothing printed

        // Option 3: Throw custom exception if not found
        // User notFound = found4.orElseThrow(() -> new RuntimeException("User missing"));
    }
}
