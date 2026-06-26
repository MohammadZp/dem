package ir.dotin.exam.corejava;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamsCollectors {

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

        @Override
        public String toString() {
            return name + "(" + grade + ")";
        }
    }

    public static void main(String[] args) {
        // Create sample data
        List<Student> students = Arrays.asList(
                new Student("Alice", "CS", 85, 20),
                new Student("Bob", "CS", 92, 22),
                new Student("Charlie", "Math", 78, 21),
                new Student("Diana", "CS", 95, 23),
                new Student("Eve", "Math", 88, 20),
                new Student("Frank", "Physics", 76, 22)
        );

        System.out.println("=== COLLECTORS.toList() EXAMPLES ===\n");
        demonstrateToList(students);

        System.out.println("\n=== COLLECTORS.groupingBy() EXAMPLES ===\n");
        demonstrateGroupingBy(students);

        System.out.println("\n=== ADVANCED GROUPINGBY EXAMPLES ===\n");
        demonstrateAdvancedGroupingBy(students);

        System.out.println("\n=== COMMON PITFALLS & EXAM TIPS ===\n");
        demonstrateCommonPitfalls();
    }

    // ============================================================
    // 1. COLLECTORS.toList() - Basic collection to List
    // ============================================================
    static void demonstrateToList(List<Student> students) {

        // EXAMPLE 1: Simple filter and collect to List
        // Collect all CS students into a List
        List<Student> csStudents = students.stream()
                .filter(s -> s.getDepartment().equals("CS"))
                .toList();  // Terminal operation - stream ends here!

        System.out.println("1. CS Students: " + csStudents);
        // Output: [Alice(85), Bob(92), Diana(95)]

        // EXAMPLE 2: Transform (map) then collect
        // Get names of all students with grade > 80
        List<String> highPerformers = students.stream()
                .filter(s -> s.getGrade() > 80)
                .map(Student::getName)          // Transform Student -> String
                .toList();   // Collect to List<String>

        System.out.println("2. High performers (grade>80): " + highPerformers);
        // Output: [Alice, Bob, Diana, Eve]

        // EXAMPLE 3: toList() with distinct elements
        List<String> uniqueDepartments = students.stream()
                .map(Student::getDepartment)
                .distinct()                      // Remove duplicates
                .toList();

        System.out.println("3. Unique departments: " + uniqueDepartments);
        // Output: [CS, Math, Physics]

        // EXAMPLE 4: toUnmodifiableList() - creates immutable list
        List<String> readOnlyNames = students.stream()
                .map(Student::getName)
                .limit(3)
                .toList(); // Can't modify!

        System.out.println("4. Unmodifiable list: " + readOnlyNames);
        // readOnlyNames.add("Test"); // Throws UnsupportedOperationException!
    }

    // ============================================================
    // 2. COLLECTORS.groupingBy() - Group elements by a key
    // ============================================================
    static void demonstrateGroupingBy(List<Student> students) {

        // EXAMPLE 1: Simple groupingBy - one argument
        // Group students by their department
        Map<String, List<Student>> studentsByDept = students.stream()
                .collect(Collectors.groupingBy(Student::getDepartment));

        System.out.println("1. Students by Department:");
        studentsByDept.forEach((dept, list) ->
                System.out.println("   " + dept + ": " + list));
        // Output: CS: [Alice(85), Bob(92), Diana(95)]
        //         Math: [Charlie(78), Eve(88)]
        //         Physics: [Frank(76)]

        // EXAMPLE 2: groupingBy with counting() - get count per group
        Map<String, Long> countByDept = students.stream()
                .collect(Collectors.groupingBy(
                        Student::getDepartment,
                        Collectors.counting()  // Downstream collector
                ));

        System.out.println("2. Count by Department: " + countByDept);
        // Output: {CS=3, Math=2, Physics=1}

        // EXAMPLE 3: groupingBy with summingInt - sum numeric values
        Map<String, Integer> totalGradesByDept = students.stream()
                .collect(Collectors.groupingBy(
                        Student::getDepartment,
                        Collectors.summingInt(Student::getGrade)
                ));

        System.out.println("3. Total grades by dept: " + totalGradesByDept);
        // Output: {CS=272, Math=166, Physics=76}

        // EXAMPLE 4: groupingBy with averagingInt
        Map<String, Double> avgGradeByDept = students.stream()
                .collect(Collectors.groupingBy(
                        Student::getDepartment,
                        Collectors.averagingInt(Student::getGrade)
                ));

        System.out.println("4. Average grade by dept: " + avgGradeByDept);
        // Output: {CS=90.666..., Math=83.0, Physics=76.0}
    }

    // ============================================================
    // 3. ADVANCED GROUPINGBY - Multiple levels & transformations
    // ============================================================
    static void demonstrateAdvancedGroupingBy(List<Student> students) {

        // EXAMPLE 1: Two-level grouping (nested groupingBy)
        // First by department, then by grade range (pass/fail)
        Map<String, Map<String, List<Student>>> multiLevel = students.stream()
                .collect(Collectors.groupingBy(
                        Student::getDepartment,
                        Collectors.groupingBy(s -> s.getGrade() >= 80 ? "Pass" : "Fail")
                ));

        System.out.println("1. Two-level grouping (Dept -> Pass/Fail):");
        multiLevel.forEach((dept, gradeMap) -> {
            System.out.println("   " + dept + ": " + gradeMap);
        });
        // Output: CS: {Pass=[Alice(85), Bob(92), Diana(95)]}
        //         Math: {Pass=[Eve(88)], Fail=[Charlie(78)]}
        //         Physics: {Fail=[Frank(76)]}

        // EXAMPLE 2: groupingBy with mapping - transform before grouping
        // Group department names, but only collect student names (not full objects)
        Map<String, List<String>> namesByDept = students.stream()
                .collect(Collectors.groupingBy(
                        Student::getDepartment,
                        Collectors.mapping(Student::getName, Collectors.toList())
                ));

        System.out.println("2. Student names by department: " + namesByDept);
        // Output: {CS=[Alice, Bob, Diana], Math=[Charlie, Eve], Physics=[Frank]}

        // EXAMPLE 3: groupingBy with maxBy - find top student per department
        Map<String, Optional<Student>> topStudentByDept = students.stream()
                .collect(Collectors.groupingBy(
                        Student::getDepartment,
                        Collectors.maxBy(Comparator.comparingInt(Student::getGrade))
                ));

        System.out.println("3. Top student per department:");
        topStudentByDept.forEach((dept, student) ->
                System.out.println("   " + dept + ": " + student.get()));
        // Output: CS: Diana(95), Math: Eve(88), Physics: Frank(76)

        // EXAMPLE 4: groupingBy with collectingAndThen - post-process results
        // Get top 2 students per department as List (not Map)
        Map<String, List<Student>> top2ByDept = students.stream()
                .collect(Collectors.groupingBy(
                        Student::getDepartment,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparingInt(Student::getGrade).reversed())
                                        .limit(2)
                                        .collect(Collectors.toList())
                        )
                ));

        System.out.println("4. Top 2 students per department: " + top2ByDept);

        // EXAMPLE 5: groupingBy with filtering (Java 9+)
        // Group students older than 21 by department
        Map<String, List<Student>> olderStudentsByDept = students.stream()
                .collect(Collectors.groupingBy(
                        Student::getDepartment,
                        Collectors.filtering(s -> s.getAge() > 21, Collectors.toList())
                ));

        System.out.println("5. Students older than 21 by dept: " + olderStudentsByDept);
    }

    // ============================================================
    // 4. COMMON PITFALLS & EXAM TIPS
    // ============================================================
    static void demonstrateCommonPitfalls() {

        // PITFALL 1: Stream cannot be reused after terminal operation
        Stream<String> stream = Stream.of("a", "b", "c");
        List<String> list1 = stream.toList();
        // List<String> list2 = stream.collect(Collectors.toList()); // ERROR! Stream already consumed

        System.out.println("1. PITFALL: Streams can't be reused after terminal operation");

        // PITFALL 2: Null keys in groupingBy throw NullPointerException
        List<String> namesWithNull = Arrays.asList("Alice", null, "Bob", null);
        try {
            Map<Integer, List<String>> byLength = namesWithNull.stream()
                    .collect(Collectors.groupingBy(String::length)); // NullPointerException!
        } catch (NullPointerException e) {
            System.out.println("2. PITFALL: groupingBy throws NPE with null keys!");
        }

        // FIX: Filter out nulls first
        Map<Integer, List<String>> safeGrouping = namesWithNull.stream()
                .filter(Objects::nonNull)  // Remove nulls
                .collect(Collectors.groupingBy(String::length));
        System.out.println("   Fixed: " + safeGrouping);

        // PITFALL 3: toList() vs toUnmodifiableList()
        List<String> mutableList = Stream.of("X", "Y").collect(Collectors.toList());
        mutableList.add("Z");  // Works fine
        System.out.println("3. mutableList after add: " + mutableList);

        List<String> immutableList = Stream.of("X", "Y").toList();
        // immutableList.add("Z"); // Throws exception!
        System.out.println("   immutableList cannot be modified");

        // TIP: Know what each collector returns
        System.out.println("\n=== EXAM CHEAT SHEET ===");
        System.out.println("• collect(Collectors.toList())     → List<T>");
        System.out.println("• collect(Collectors.toSet())      → Set<T>");
        System.out.println("• collect(Collectors.toMap(...))   → Map<K,V>");
        System.out.println("• collect(groupingBy(f))           → Map<K, List<T>>");
        System.out.println("• collect(groupingBy(f, counting())) → Map<K, Long>");
        System.out.println("• collect(partitioningBy(p))       → Map<Boolean, List<T>>");
    }
}
