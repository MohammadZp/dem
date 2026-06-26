package ir.dotin.exam.corejava;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * COMPLETE GUIDE TO GENERICS, WILDCARDS, AND BOUNDS
 * <p>
 * This class contains EVERYTHING you need for your exam:
 * - Basic Generics
 * - Type Bounds (Upper, Lower, Multiple)
 * - Wildcards (?, extends, super)
 * - PECS Rule
 * - Common Patterns and Pitfalls
 */
public class CompleteGenericsGuide {

    // ============================================================
    // PART 1: BASIC GENERICS - WHY WE NEED THEM
    // ============================================================

    static class Part1_BasicGenerics {

        // WITHOUT Generics - DANGEROUS!
        @SuppressWarnings("unchecked")
        static void withoutGenerics() {
            List list = new ArrayList();  // Raw type - can hold anything
            list.add("Hello");
            list.add(123);
            list.add(new Object());

            // This compiles but crashes at runtime!
            String s = (String) list.get(0);  // OK
            // String s2 = (String) list.get(1);  // 💥 ClassCastException at runtime!
        }

        // WITH Generics - TYPE SAFE
        static void withGenerics() {
            List<String> list = new ArrayList<>();  // Can only hold Strings
            list.add("Hello");
            list.add("World");
            // list.add(123);  // ❌ COMPILE ERROR! Can't add Integer

            String s = list.get(0);  // No cast needed!
            System.out.println("With generics: " + s);
        }

        // Generic Class
        static class Box<T> {
            private T value;

            void set(T value) {
                this.value = value;
            }

            T get() {
                return value;
            }
        }

        // Generic Method
        static <T> T identity(T value) {
            return value;
        }

        static void demonstrate() {
            Box<String> stringBox = new Box<>();
            stringBox.set("Hello");
            System.out.println("Box contains: " + stringBox.get());

            Box<Integer> intBox = new Box<>();
            intBox.set(123);
            System.out.println("Box contains: " + intBox.get());

            String s = identity("test");
            Integer i = identity(123);
            System.out.println("Identity: " + s + ", " + i);
        }
    }

    // ============================================================
    // PART 2: UPPER BOUND - extends (IS A relationship)
    // ============================================================

    static class Part2_UpperBound {

        /**
         * UPPER BOUND: <? extends T> or <T extends Class>
         * <p>
         * Meaning: "IS A T" - T or ANY subclass of T
         * <p>
         * Rules:
         * - ✅ Can READ as T
         * - ❌ Cannot WRITE (except null)
         * - Used when you PRODUCE/READ data
         */

        // Example 1: Generic class with upper bound
        record NumberContainer<T extends Number>(T value) {

            double getDoubleValue() {
                return value.doubleValue();  // Safe because T extends Number
            }
        }

        // Example 2: Method with upper bound wildcard
        static double sumOfList(List<? extends Number> list) {
            double sum = 0.0;
            for (Number n : list) {  // Can read as Number
                sum += n.doubleValue();
            }
            return sum;
        }

        // Example 3: What you CAN and CANNOT do
        static void demonstrateOperations() {
            List<? extends Number> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

            // ✅ CAN READ
            Number first = numbers.get(0);
            System.out.println("First element: " + first);

            // ✅ CAN ITERATE
            for (Number n : numbers) {
                System.out.print(n + " ");
            }
            System.out.println();

            // ✅ CAN CHECK SIZE
            int size = numbers.size();
            System.out.println("Size: " + size);

            // ✅ CAN REMOVE
            // numbers.remove(0);  // Actually works! (remove is allowed)

            // ❌ CANNOT ADD (except null)
            // numbers.add(10);     // COMPILE ERROR
            // numbers.add(10.5);   // COMPILE ERROR
            numbers.add(null);      // Only null is allowed
        }

        static void demonstrate() {
            System.out.println("\n--- Upper Bound Examples ---");

            // Works with Integer (subclass of Number)
            List<Integer> ints = Arrays.asList(1, 2, 3, 4, 5);
            System.out.println("Sum of ints: " + sumOfList(ints));

            // Works with Double (subclass of Number)
            List<Double> doubles = Arrays.asList(1.5, 2.5, 3.5);
            System.out.println("Sum of doubles: " + sumOfList(doubles));

            // Works with Number itself
            List<Number> numbers = Arrays.asList(1, 2.5, 3, 4.5);
            System.out.println("Sum of numbers: " + sumOfList(numbers));

            // Generic class with upper bound
            NumberContainer<Integer> intContainer = new NumberContainer<>(100);
            NumberContainer<Double> doubleContainer = new NumberContainer<>(3.14159);
            System.out.println("Int double value: " + intContainer.getDoubleValue());
            System.out.println("Double double value: " + doubleContainer.getDoubleValue());

            demonstrateOperations();
        }
    }

    // ============================================================
    // PART 3: LOWER BOUND - super (CAN HOLD relationship)
    // ============================================================

    static class Part3_LowerBound {

        /**
         * LOWER BOUND: <? super T>
         * <p>
         * Meaning: "CAN HOLD T" - T or ANY superclass of T
         * <p>
         * Rules:
         * - ✅ Can WRITE T and subtypes of T
         * - ✅ Can READ but only as Object
         * - Used when you CONSUME/WRITE data
         */

        // Example: Method that adds integers to any compatible list
        static void addIntegers(List<? super Integer> list, int count) {
            for (int i = 1; i <= count; i++) {
                list.add(i);  // Can add Integer
            }
            // list.add(10.5);  // ❌ Cannot add Double
        }

        // Example: Method that adds to any list that can hold strings
        static void addStrings(List<? super String> list, String... strings) {
            for (String s : strings) {
                list.add(s);
            }
        }

        static void demonstrateOperations() {
            List<? super Integer> list = new ArrayList<Number>();

            // ✅ CAN ADD Integer
            list.add(10);
            list.add(20);
            list.add(30);

            // ✅ CAN ADD null
            list.add(null);

            // ❌ CANNOT add other numeric types
            // list.add(10.5);   // COMPILE ERROR

            // ✅ CAN READ but ONLY as Object
            Object obj = list.get(0);
            System.out.println("First element as Object: " + obj);

            // ❌ CANNOT read as Integer (don't know exact type)
            // Integer i = list.get(0);  // COMPILE ERROR

            // Can iterate as Object
            for (Object o : list) {
                System.out.print(o + " ");
            }
            System.out.println();
        }

        static void demonstrate() {
            System.out.println("\n--- Lower Bound Examples ---");

            // Works with List<Integer>
            List<Integer> intList = new ArrayList<>();
            addIntegers(intList, 5);
            System.out.println("Integer list: " + intList);

            // Works with List<Number>
            List<Number> numList = new ArrayList<>();
            addIntegers(numList, 3);
            System.out.println("Number list: " + numList);

            // Works with List<Object>
            List<Object> objList = new ArrayList<>();
            addIntegers(objList, 4);
            System.out.println("Object list: " + objList);

            // String example
            List<Object> objects = new ArrayList<>();
            addStrings(objects, "A", "B", "C");
            System.out.println("Strings in Object list: " + objects);

            demonstrateOperations();
        }
    }

    // ============================================================
    // PART 4: MULTIPLE BOUNDS (Class & Interface)
    // ============================================================

    static class Part4_MultipleBounds {

        /**
         * MULTIPLE BOUNDS: <T extends Class & Interface1 & Interface2>
         * <p>
         * Rules:
         * - Class MUST come FIRST
         * - Can have multiple interfaces (separated by &)
         * - Cannot extend multiple classes
         */

        // Interfaces
        interface Drawable {
            void draw();
        }

        interface Measurable {
            double getArea();
        }

        // Abstract class
        static abstract class Shape {
            abstract String getName();
        }

        // T must extend Shape AND implement Drawable AND Measurable
        static class Canvas<T extends Shape & Drawable & Measurable> {
            private T shape;

            Canvas(T shape) {
                this.shape = shape;
            }

            void display() {
                System.out.println("Drawing: " + shape.getName());
                shape.draw();
                System.out.println("Area: " + shape.getArea());
            }
        }

        // Concrete class implementing all requirements
        static class Circle extends Shape implements Drawable, Measurable {
            double radius;

            Circle(double radius) {
                this.radius = radius;
            }

            @Override
            String getName() {
                return "Circle";
            }

            @Override
            public void draw() {
                System.out.println("  ⚪ Drawing circle");
            }

            @Override
            public double getArea() {
                return Math.PI * radius * radius;
            }
        }

        // Generic method with multiple bounds
        static <T extends Comparable<T> & Serializable> T max(T a, T b) {
            return a.compareTo(b) > 0 ? a : b;
        }

        static void demonstrate() {
            System.out.println("\n--- Multiple Bounds Examples ---");

            Circle circle = new Circle(5);
            Canvas<Circle> canvas = new Canvas<>(circle);
            canvas.display();

            // Works with Integer (implements Comparable & Serializable)
            Integer maxInt = max(10, 20);
            System.out.println("Max int: " + maxInt);

            // Works with String (implements Comparable & Serializable)
            String maxStr = max("Apple", "Banana");
            System.out.println("Max string: " + maxStr);

            // This would NOT compile:
            // Object obj = max(new Object(), new Object());  // Object doesn't implement Comparable
        }
    }

    // ============================================================
    // PART 5: UNBOUNDED WILDCARD <?>
    // ============================================================

    static class Part5_UnboundedWildcard {

        /**
         * UNBOUNDED WILDCARD: <?>
         * <p>
         * Meaning: "Unknown type" - can be ANY type
         * <p>
         * Rules:
         * - ✅ Can READ as Object
         * - ❌ Cannot WRITE (except null)
         * - Used when type doesn't matter
         */

        // Print any list
        static void printList(List<?> list) {
            for (Object item : list) {
                System.out.println("  " + item);
            }
        }

        // Check if list contains null
        static boolean hasNull(List<?> list) {
            for (Object item : list) {
                if (item == null) return true;
            }
            return false;
        }

        static void demonstrate() {
            System.out.println("\n--- Unbounded Wildcard Examples ---");

            List<String> strings = Arrays.asList("A", "B", "C");
            List<Integer> integers = Arrays.asList(1, 2, 3);
            List<Double> doubles = Arrays.asList(1.1, 2.2, 3.3);

            System.out.println("Printing strings:");
            printList(strings);

            System.out.println("Printing integers:");
            printList(integers);

            System.out.println("Printing doubles:");
            printList(doubles);

            // What you CANNOT do:
            List<?> list = new ArrayList<String>();
            // list.add("Hello");  // ❌ COMPILE ERROR
            list.add(null);        // ✅ Only null allowed

            System.out.println("Has null? " + hasNull(Arrays.asList("A", null, "C")));
        }
    }

    // ============================================================
    // PART 6: PECS RULE (Producer Extends, Consumer Super)
    // ============================================================

    static class Part6_PECS {

        /**
         * PECS = Producer Extends, Consumer Super
         * <p>
         * - If you PRODUCE (read) data from collection → use <? extends T>
         * - If you CONSUME (write) data to collection → use <? super T>
         * - If you do BOTH → use concrete type <T>
         */

        // PRODUCER: Reading from list → use EXTENDS
        static <T> T getFirst(List<? extends T> producer) {
            // We are PRODUCING/READING data from the list
            return producer.get(0);
        }

        // PRODUCER: Copy from source (reading)
        static <T> void copyFrom(List<? extends T> source, List<T> dest) {
            for (T item : source) {
                dest.add(item);
            }
        }

        // CONSUMER: Writing to list → use SUPER
        static <T> void addAll(List<? super T> consumer, T... items) {
            // We are CONSUMING/WRITING data to the list
            for (T item : items) {
                consumer.add(item);
            }
        }

        // BOTH: Reading AND Writing → use concrete type
        static <T> void swap(List<T> list, int i, int j) {
            T temp = list.get(i);  // Reading
            list.set(i, list.get(j));  // Writing
            list.set(j, temp);  // Writing
        }

        // Classic example: Copy between lists
        static <T> void copy(List<? extends T> source, List<? super T> dest) {
            // source: we READ from → extends
            // dest: we WRITE to → super
            for (T item : source) {
                dest.add(item);
            }
        }

        static void demonstrate() {
            System.out.println("\n--- PECS Rule Examples ---");

            // Producer example
            List<Integer> ints = Arrays.asList(1, 2, 3, 4, 5);
            Integer first = getFirst(ints);
            System.out.println("First element: " + first);

            // Consumer example
            List<Number> numbers = new ArrayList<>();
            addAll(numbers, 10, 20, 30, 40, 50);
            System.out.println("After addAll: " + numbers);

            // Copy example (both)
            List<Integer> source = Arrays.asList(1, 2, 3);
            List<Number> dest = new ArrayList<>();
            copy(source, dest);
            System.out.println("Copied: " + dest);

            // Swap example (both read and write)
            List<String> names = new ArrayList<>(Arrays.asList("Alice", "Bob", "Charlie"));
            swap(names, 0, 2);
            System.out.println("After swap: " + names);
        }
    }

    // ============================================================
    // PART 7: COMPARISON TABLE (UPPER vs LOWER)
    // ============================================================

    static class Part7_ComparisonTable {

        static void demonstrate() {
            System.out.println("\n--- UPPER BOUND vs LOWER BOUND COMPARISON ---");
            System.out.println("┌─────────────────────┬──────────────────────┬──────────────────────┐");
            System.out.println("│                     │ UPPER BOUND          │ LOWER BOUND          │");
            System.out.println("│                     │ <? extends T>        │ <? super T>          │");
            System.out.println("├─────────────────────┼──────────────────────┼──────────────────────┤");
            System.out.println("│ Meaning             │ T or SUBCLASS of T   │ T or SUPERCLASS of T │");
            System.out.println("├─────────────────────┼──────────────────────┼──────────────────────┤");
            System.out.println("│ Can READ?           │ ✅ YES (as T)         │ ✅ YES (as Object)    │");
            System.out.println("├─────────────────────┼──────────────────────┼──────────────────────┤");
            System.out.println("│ Can WRITE?          │ ❌ NO (except null)   │ ✅ YES (T and below)  │");
            System.out.println("├─────────────────────┼──────────────────────┼──────────────────────┤");
            System.out.println("│ PECS Role           │ PRODUCER              │ CONSUMER              │");
            System.out.println("├─────────────────────┼──────────────────────┼──────────────────────┤");
            System.out.println("│ Example with Number │ List<Integer>        │ List<Object>          │");
            System.out.println("│                     │ List<Double>         │ List<Number>          │");
            System.out.println("│                     │ List<Number>         │ List<Integer>         │");
            System.out.println("└─────────────────────┴──────────────────────┴──────────────────────┘");
        }
    }

    // ============================================================
    // PART 8: TYPE ERASURE (What Happens at Runtime)
    // ============================================================

    static class Part8_TypeErasure {

        /**
         * TYPE ERASURE: Generic type information is REMOVED at runtime
         * <p>
         * Important exam facts:
         * - Generics are a compile-time feature only
         * - At runtime, List<String> becomes just List
         * - Cannot use instanceof with generic types
         * - Cannot create arrays of generic types
         */

        @SuppressWarnings("unchecked")
        static void demonstrate() {
            System.out.println("\n--- Type Erasure ---");

            List<String> strings = new ArrayList<>();
            List<Integer> integers = new ArrayList<>();

            // At RUNTIME, both are just ArrayList
            System.out.println("strings.getClass() == integers.getClass(): "
                    + (strings.getClass() == integers.getClass()));  // true!

            // Cannot use instanceof with generic types:
            // if (strings instanceof List<String>)  // ❌ COMPILE ERROR!

            // Workaround: use wildcard
            if (strings instanceof List<?>) {
                System.out.println("strings is a List");
            }

            // Bridge methods (automatically created by compiler)
            // When a subclass overrides a generic method, compiler creates bridge methods
        }
    }

    // ============================================================
    // PART 9: COMMON MISTAKES AND PITFALLS
    // ============================================================

    static class Part9_CommonMistakes {

        @SuppressWarnings({"unchecked", "rawtypes"})
        static void demonstrate() {
            System.out.println("\n--- COMMON MISTAKES TO AVOID ---");

            // ❌ MISTAKE 1: Cannot instantiate with wildcard
            // List<?> list = new ArrayList<?>();  // COMPILE ERROR!
            List<?> list = new ArrayList<String>();  // ✅ Right side must be concrete

            // ❌ MISTAKE 2: Cannot create array of generic type
            // List<String>[] array = new List<String>[10];  // COMPILE ERROR!
            List<String>[] array = (List<String>[]) new List[10];  // ✅ Workaround (unsafe)

            // ❌ MISTAKE 3: Cannot use primitive types as type arguments
            // List<int> ints = new ArrayList<>();  // COMPILE ERROR!
            List<Integer> ints = new ArrayList<>();  // ✅ Use wrapper class

            // ❌ MISTAKE 4: Cannot have static field of generic type
            class Bad<T> {
                // static T value;  // COMPILE ERROR!
                static Object value;  // ✅ Workaround
            }

            // ❌ MISTAKE 5: Cannot catch generic exception
            // try { } catch (T e) { }  // COMPILE ERROR!

            // ❌ MISTAKE 6: Adding to unbounded wildcard
            List<?> unbounded = new ArrayList<String>();
            // unbounded.add("Hello");  // COMPILE ERROR!
            unbounded.add(null);  // ✅ Only null allowed

            // ❌ MISTAKE 7: Using raw types
            List raw = new ArrayList();  // Raw type - DANGEROUS!
            raw.add(123);
            raw.add("String");
            // Don't do this!

            System.out.println("⚠️ Avoid these common mistakes in your exam!");
        }
    }

    // ============================================================
    // PART 10: REAL-WORLD COMPLETE EXAMPLE
    // ============================================================

    static class Part10_RealWorldExample {

        // Generic repository pattern
        interface Repository<T, ID> {
            T findById(ID id);

            List<T> findAll();

            void save(T entity);

            void delete(T entity);
        }

        // Generic service with bounds
        static class UserService<T extends User> {
            private List<T> users = new ArrayList<>();

            void add(T user) {
                users.add(user);
            }

            T findMax() {
                return users.stream()
                        .max(Comparable::compareTo)
                        .orElse(null);
            }

            List<? extends T> getUsers() {  // Producer - returns data
                return users;
            }

            void addAll(List<? super T> consumer, T... items) {  // Consumer - accepts data
                for (T item : items) {
                    consumer.add(item);
                }
            }
        }

        static class User implements Comparable<User> {
            String name;
            int age;

            User(String name, int age) {
                this.name = name;
                this.age = age;
            }

            @Override
            public int compareTo(User other) {
                return Integer.compare(this.age, other.age);
            }

            @Override
            public String toString() {
                return name + " (" + age + ")";
            }
        }

        static void demonstrate() {
            System.out.println("\n--- REAL-WORLD COMPLETE EXAMPLE ---");

            UserService<User> service = new UserService<>();
            service.add(new User("Alice", 25));
            service.add(new User("Bob", 30));
            service.add(new User("Charlie", 20));

            System.out.println("Oldest user: " + service.findMax());

            // Producer: getting users
            List<? extends User> users = service.getUsers();
            for (User u : users) {
                System.out.println("User: " + u);
            }

            // Consumer: adding users
            List<Object> container = new ArrayList<>();
            service.addAll(container, new User("David", 35), new User("Eve", 28));
            System.out.println("Container: " + container);
        }
    }

    // ============================================================
    // CHEAT SHEET SUMMARY
    // ============================================================

    static class CheatSheet {
        static void print() {
            System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                    GENERICS CHEAT SHEET FOR EXAM                    ║");
            System.out.println("╠════════════════════════════════════════════════════════════════════╣");
            System.out.println("║                                                                     ║");
            System.out.println("║  SYNTAX                    MEANING                    USAGE         ║");
            System.out.println("║  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━║");
            System.out.println("║  <T>                      Any type                    Basic generic ║");
            System.out.println("║  <T extends Number>       T is Number or subclass    Upper bound    ║");
            System.out.println("║  <?>                      Unknown type                Type unknown   ║");
            System.out.println("║  <? extends T>            T or subclass (IS A)        Producer/Read  ║");
            System.out.println("║  <? super T>              T or superclass (CAN HOLD)  Consumer/Write ║");
            System.out.println("║  <T extends A & B>        T must extend A and B       Multiple bounds║");
            System.out.println("║                                                                     ║");
            System.out.println("║  PECS RULE: Producer EXTENDS, Consumer SUPER                         ║");
            System.out.println("║                                                                     ║");
            System.out.println("║  QUICK MEMORY:                                                     ║");
            System.out.println("║    - GET data (read)  → use EXTENDS                                 ║");
            System.out.println("║    - PUT data (write) → use SUPER                                   ║");
            System.out.println("║                                                                     ║");
            System.out.println("║  GENERIC METHOD VS WILDCARD:                                        ║");
            System.out.println("║    <T> void method(List<T>)     - Can read AND write               ║");
            System.out.println("║    void method(List<?>)         - Can only read as Object          ║");
            System.out.println("║                                                                     ║");
            System.out.println("╚════════════════════════════════════════════════════════════════════╝");
        }
    }

    // ============================================================
    // MAIN METHOD - RUN EVERYTHING
    // ============================================================

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║         COMPLETE GENERICS GUIDE - EXAM PREPARATION                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");

        Part1_BasicGenerics.demonstrate();
        Part2_UpperBound.demonstrate();
        Part3_LowerBound.demonstrate();
        Part4_MultipleBounds.demonstrate();
        Part5_UnboundedWildcard.demonstrate();
        Part6_PECS.demonstrate();
        Part7_ComparisonTable.demonstrate();
        Part8_TypeErasure.demonstrate();
        Part9_CommonMistakes.demonstrate();
        Part10_RealWorldExample.demonstrate();
        CheatSheet.print();

        System.out.println("\n✅ All examples completed! Review each section for your exam.");
        System.out.println("🎯 Focus on: PECS Rule, Upper vs Lower bounds, Wildcard usage");
    }
}
