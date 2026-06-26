package ir.dotin.exam.corejava;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Complete demonstration of all built-in functional interfaces in Java
 * Exam-focused examples with detailed comments
 */
public class FunctionalInterfacesDemo {

    // Helper class for examples
    static class Student {
        String name;
        String department;
        int grade;
        int age;

        Student(String name, String department, int grade, int age) {
            this.name = name;
            this.department = department;
            this.grade = grade;
            this.age = age;
        }

        String getName() { return name; }
        String getDepartment() { return department; }
        int getGrade() { return grade; }
        int getAge() { return age; }

        void setGrade(int grade) { this.grade = grade; }

        @Override
        public String toString() {
            return name + "(" + grade + ")";
        }
    }

    public static void main(String[] args) {
        System.out.println("========== FUNCTIONAL INTERFACES DEMO ==========\n");

        demonstratePredicate();
        demonstrateFunction();
        demonstrateConsumer();
        demonstrateSupplier();
        demonstrateBiFunction();
        demonstrateUnaryBinaryOperator();
        demonstratePrimitiveFunctionalInterfaces();
        demonstrateCustomFunctionalInterface();
    }

    // ============================================================
    // 1. PREDICATE<T> - boolean test(T t)
    // Use: Testing conditions, filtering
    // ============================================================
    static void demonstratePredicate() {
        System.out.println("--- 1. PREDICATE<T> ---");
        System.out.println("Method: boolean test(T t)");
        System.out.println("Use: Test a condition\n");

        List<Student> students = getSampleStudents();

        // Basic Predicate
        Predicate<Student> isCSE = student -> student.getDepartment().equals("CS");
        Predicate<Student> hasHighGrade = student -> student.getGrade() >= 85;
        Predicate<Student> isAdult = student -> student.getAge() >= 21;

        // Combining predicates with and(), or(), negate()
        Predicate<Student> csHighPerformer = isCSE.and(hasHighGrade);
        Predicate<Student> csAdult = isCSE.and(isAdult);
        Predicate<Student> notCs = isCSE.negate();

        System.out.println("CS Students with high grade (>=85):");
        students.stream()
                .filter(csHighPerformer)
                .forEach(s -> System.out.println("  " + s));

        System.out.println("\nCS Students who are adults (>=21):");
        students.stream()
                .filter(csAdult)
                .forEach(s -> System.out.println("  " + s));

        System.out.println("\nNon-CS Students:");
        students.stream()
                .filter(notCs)
                .forEach(s -> System.out.println("  " + s));

        // Predicate with test() method
        Predicate<String> isEmpty = String::isEmpty;
        Predicate<String> isBlank = s -> s == null || s.trim().isEmpty();

        System.out.println("\nString tests:");
        System.out.println("  'Hello' is empty? " + isEmpty.test("Hello"));  // false
        System.out.println("  '' is empty? " + isEmpty.test(""));            // true
        System.out.println("  '   ' is blank? " + isBlank.test("   "));      // true
    }

    // ============================================================
    // 2. FUNCTION<T,R> - R apply(T t)
    // Use: Transform one value to another
    // ============================================================
    static void demonstrateFunction() {
        System.out.println("\n\n--- 2. FUNCTION<T,R> ---");
        System.out.println("Method: R apply(T t)");
        System.out.println("Use: Transform T into R\n");

        List<Student> students = getSampleStudents();

        // Basic Function: Student -> String (name)
        Function<Student, String> getName = Student::getName;

        // Function: Student -> Integer (grade)
        Function<Student, Integer> getGrade = Student::getGrade;

        // Function chaining with andThen() and compose()
        Function<String, String> addTitle = name -> "Student: " + name;
        Function<Integer, String> gradeToLetter = grade -> {
            if (grade >= 90) return "A";
            if (grade >= 80) return "B";
            if (grade >= 70) return "C";
            return "F";
        };

        // Chain: Student -> name -> add title
        Function<Student, String> getTitledName = getName.andThen(addTitle);

        // Chain: Student -> grade -> letter grade
        Function<Student, String> getLetterGrade = getGrade.andThen(gradeToLetter);

        System.out.println("Student names with titles:");
        students.stream()
                .map(getTitledName)
                .forEach(name -> System.out.println("  " + name));

        System.out.println("\nStudent letter grades:");
        students.forEach(s ->
                System.out.println("  " + s.getName() + ": " + getLetterGrade.apply(s)));

        // identity() - returns same value
        Function<String, String> identity = Function.identity();
        System.out.println("\nIdentity function: " + identity.apply("Same value"));
    }

    // ============================================================
    // 3. CONSUMER<T> - void accept(T t)
    // Use: Perform an operation without returning a value
    // ============================================================
    static void demonstrateConsumer() {
        System.out.println("\n\n--- 3. CONSUMER<T> ---");
        System.out.println("Method: void accept(T t)");
        System.out.println("Use: Perform operation with side effect\n");

        List<Student> students = getSampleStudents();

        // Basic Consumer: print student
        Consumer<Student> printStudent = s -> System.out.println("  " + s);

        // Consumer: give bonus points
        Consumer<Student> giveBonus = s -> s.setGrade(s.getGrade() + 5);

        // Consumer: print after modification
        Consumer<Student> printWithBonus = giveBonus.andThen(printStudent);

        System.out.println("Students after bonus (+5 points):");
        students.stream()
                .limit(3)  // First 3 students only
                .forEach(printWithBonus);

        // Consumer chaining with andThen()
        Consumer<String> print = s -> System.out.print(s);
        Consumer<String> printNewLine = s -> System.out.println();

        Consumer<String> printLine = print.andThen(printNewLine);

        System.out.println("\nConsumer chaining example:");
        printLine.accept("This prints and then adds new line");
    }

    // ============================================================
    // 4. SUPPLIER<T> - T get()
    // Use: Produce/generate values without input
    // ============================================================
    static void demonstrateSupplier() {
        System.out.println("\n\n--- 4. SUPPLIER<T> ---");
        System.out.println("Method: T get()");
        System.out.println("Use: Generate or supply values\n");

        // Basic Supplier: generate random numbers
        Supplier<Double> randomSupplier = Math::random;

        // Supplier: create new ArrayList
        Supplier<List<String>> listSupplier = ArrayList::new;

        // Supplier: generate sequential IDs
        Supplier<Integer> idSupplier = new Supplier<>() {
            int id = 1;
            @Override
            public Integer get() {
                return id++;
            }
        };

        System.out.println("Random numbers:");
        for (int i = 0; i < 3; i++) {
            System.out.printf("  %.4f%n", randomSupplier.get());
        }

        System.out.println("\nGenerated IDs:");
        for (int i = 0; i < 5; i++) {
            System.out.println("  ID: " + idSupplier.get());
        }

        // Using supplier with stream generation
        List<String> newList = listSupplier.get();
        newList.add("Created by supplier");
        System.out.println("\nList created by supplier: " + newList);

        // Lazy initialization pattern
        Supplier<ExpensiveObject> lazyObject = () -> new ExpensiveObject();
        System.out.println("\nLazy initialization: Object not created yet");
        System.out.println("Now calling get(): " + lazyObject.get());
    }

    // Helper for lazy initialization example
    static class ExpensiveObject {
        ExpensiveObject() {
            System.out.println("  (ExpensiveObject constructor called)");
        }
        public String toString() { return "ExpensiveObject instance"; }
    }

    // ============================================================
    // 5. BIFUNCTION<T,U,R> - R apply(T t, U u)
    // Use: Transform two inputs into one output
    // ============================================================
    static void demonstrateBiFunction() {
        System.out.println("\n\n--- 5. BIFUNCTION<T,U,R> ---");
        System.out.println("Method: R apply(T t, U u)");
        System.out.println("Use: Combine two values into one\n");

        // Basic BiFunction: add two numbers
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

        // BiFunction: create Student from name and grade
        BiFunction<String, Integer, Student> createStudent =
                (name, grade) -> new Student(name, "CS", grade, 20);

        // BiFunction: combine strings
        BiFunction<String, String, String> combine =
                (first, last) -> first + " " + last;

        System.out.println("Add: " + add.apply(5, 3));  // 8

        Student newStudent = createStudent.apply("John", 88);
        System.out.println("Created student: " + newStudent);

        String fullName = combine.apply("Alice", "Johnson");
        System.out.println("Combined name: " + fullName);

        // BiFunction with andThen()
        Function<Integer, String> toString = Object::toString;
        BiFunction<Integer, Integer, String> addAndConvert = add.andThen(toString);

        System.out.println("Add and convert to string: " + addAndConvert.apply(10, 20));
    }

    // ============================================================
    // 6. UNARYOPERATOR<T> & BINARYOPERATOR<T>
    // Special cases of Function and BiFunction (same input/output type)
    // ============================================================
    static void demonstrateUnaryBinaryOperator() {
        System.out.println("\n\n--- 6. UNARYOPERATOR<T> & BINARYOPERATOR<T> ---");
        System.out.println("UnaryOperator: T apply(T t) - same type in/out");
        System.out.println("BinaryOperator: T apply(T t1, T t2) - combines two into one\n");

        // UnaryOperator: double a number
        UnaryOperator<Integer> doubleIt = x -> x * 2;

        // UnaryOperator: to uppercase
        UnaryOperator<String> toUpper = String::toUpperCase;

        // BinaryOperator: sum
        BinaryOperator<Integer> sum = (a, b) -> a + b;

        // BinaryOperator: max
        BinaryOperator<Integer> max = BinaryOperator.maxBy(Comparator.naturalOrder());

        // BinaryOperator: min
        BinaryOperator<Integer> min = BinaryOperator.minBy(Comparator.naturalOrder());

        System.out.println("Double 5: " + doubleIt.apply(5));           // 10
        System.out.println("Uppercase 'hello': " + toUpper.apply("hello")); // HELLO
        System.out.println("Sum 10 + 20: " + sum.apply(10, 20));        // 30
        System.out.println("Max of 15 and 25: " + max.apply(15, 25));   // 25
        System.out.println("Min of 15 and 25: " + min.apply(15, 25));   // 15

        // BinaryOperator with reduce
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        Integer total = numbers.stream()
                .reduce(0, sum);
        System.out.println("\nReduce sum: " + total);  // 15
    }

    // ============================================================
    // 7. PRIMITIVE FUNCTIONAL INTERFACES (Avoid autoboxing overhead)
    // IntPredicate, LongPredicate, DoublePredicate
    // IntFunction, IntToDoubleFunction, etc.
    // ============================================================
    static void demonstratePrimitiveFunctionalInterfaces() {
        System.out.println("\n\n--- 7. PRIMITIVE FUNCTIONAL INTERFACES ---");
        System.out.println("Use: Work with primitives to avoid autoboxing overhead\n");

        // IntPredicate - predicate for int
        IntPredicate isEven = n -> n % 2 == 0;
        IntPredicate isPositive = n -> n > 0;

        System.out.println("IntPredicate examples:");
        System.out.println("  Is 4 even? " + isEven.test(4));      // true
        System.out.println("  Is -5 positive? " + isPositive.test(-5)); // false

        // IntFunction - takes int, returns object
        IntFunction<String> intToString = i -> "Number: " + i;
        System.out.println("\nIntFunction: " + intToString.apply(42));

        // ToIntFunction - takes object, returns int
        ToIntFunction<String> stringLength = String::length;
        System.out.println("ToIntFunction: length of 'Java' = " + stringLength.applyAsInt("Java"));

        // IntBinaryOperator - two ints → int
        IntBinaryOperator multiply = (a, b) -> a * b;
        System.out.println("IntBinaryOperator: 5 * 6 = " + multiply.applyAsInt(5, 6));

        // LongSupplier - supplies long values
        LongSupplier timeSupplier = System::currentTimeMillis;
        System.out.println("\nLongSupplier: current time = " + timeSupplier.getAsLong());

        // DoubleConsumer - consumes double
        DoubleConsumer printDouble = d -> System.out.printf("  Value: %.2f%n", d);
        System.out.println("DoubleConsumer:");
        printDouble.accept(3.14159);

        // Practical example: sum of even numbers using primitive streams
        int[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int sumOfEvens = Arrays.stream(numbers)
                .filter(n -> n % 2 == 0)  // IntPredicate
                .sum();                    // Primitive sum

        System.out.println("\nSum of even numbers (1-10): " + sumOfEvens);
    }

    // ============================================================
    // 8. CUSTOM FUNCTIONAL INTERFACE
    // Must have EXACTLY ONE abstract method
    // Can have default and static methods
    // ============================================================
    static void demonstrateCustomFunctionalInterface() {
        System.out.println("\n\n--- 8. CUSTOM FUNCTIONAL INTERFACE ---");
        System.out.println("Rules: @FunctionalInterface annotation (optional but recommended)");
        System.out.println("       Exactly ONE abstract method\n");

        // Using custom functional interface
        TriFunction<String, String, String, String> concatenate =
                (s1, s2, s3) -> s1 + s2 + s3;

        QuadFunction<Integer, Integer, Integer, Integer, Integer> sumFour =
                (a, b, c, d) -> a + b + c + d;

        String result = concatenate.apply("Hello ", "World ", "!");
        System.out.println("TriFunction result: " + result);

        int sum = sumFour.apply(1, 2, 3, 4);
        System.out.println("QuadFunction result: " + sum);

        // Custom validator interface
        Validator<String> notEmpty = s -> s != null && !s.isEmpty();
        Validator<String> minLength = s -> s.length() >= 5;

        System.out.println("\nValidator examples:");
        System.out.println("  'Hello' not empty? " + notEmpty.validate("Hello"));
        System.out.println("  'Hi' min length 5? " + minLength.validate("Hi"));
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================
    static List<Student> getSampleStudents() {
        return Arrays.asList(
                new Student("Alice", "CS", 85, 20),
                new Student("Bob", "CS", 92, 22),
                new Student("Charlie", "Math", 78, 21),
                new Student("Diana", "CS", 95, 23),
                new Student("Eve", "Math", 88, 20),
                new Student("Frank", "Physics", 76, 22)
        );
    }
}

// ============================================================
// CUSTOM FUNCTIONAL INTERFACES
// ============================================================

/**
 * Custom functional interface with 3 parameters
 */
@FunctionalInterface
interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);

    // Can have default methods
    default TriFunction<T, U, V, R> andThen(Function<? super R, ? extends R> after) {
        return (t, u, v) -> after.apply(apply(t, u, v));
    }

    // Can have static methods
    static <T, U, V, R> TriFunction<T, U, V, R> of(TriFunction<T, U, V, R> function) {
        return function;
    }
}

/**
 * Custom functional interface with 4 parameters
 */
@FunctionalInterface
interface QuadFunction<T, U, V, W, R> {
    R apply(T t, U u, V v, W w);
}

/**
 * Custom validator functional interface
 */
@FunctionalInterface
interface Validator<T> {
    boolean validate(T value);

    // Default method for combining validators with AND
    default Validator<T> and(Validator<T> other) {
        return value -> this.validate(value) && other.validate(value);
    }

    // Default method for combining validators with OR
    default Validator<T> or(Validator<T> other) {
        return value -> this.validate(value) || other.validate(value);
    }
}
