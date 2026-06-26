package ir.dotin.exam.corejava;

import java.util.*;
import java.util.stream.*;

public class StudentsWithLambdas {

    static class Student {
        String name;
        int grade;
        String department;

        Student(String name, int grade, String department) {
            this.name = name;
            this.grade = grade;
            this.department = department;
        }

        String getName() { return name; }
        int getGrade() { return grade; }
        String getDepartment() { return department; }

        public String toString() { return name + " (" + grade + ")"; }
    }

    public static void main(String[] args) {

        List<Student> students = Arrays.asList(
                new Student("Alice", 85, "CS"),
                new Student("Bob", 92, "CS"),
                new Student("Charlie", 78, "Math"),
                new Student("Diana", 95, "CS"),
                new Student("Eve", 88, "Math"),
                new Student("Frank", 76, "Physics")
        );

        // QUESTION 1: Get all CS students with grade > 85
        System.out.println("=== CS students with grade > 85 ===");
        students.stream()
                .filter(s -> s.getDepartment().equals("CS"))
                .filter(s -> s.getGrade() > 85)
                .map(Student::getName)
                .forEach(System.out::println);
        // Output: Bob, Diana

        // QUESTION 2: Get average grade of all students
        System.out.println("\n=== Average grade ===");
        double avg = students.stream()
                .mapToInt(Student::getGrade)
                .average()
                .orElse(0);
        System.out.println("Average: " + avg);

        // QUESTION 3: Get top 2 highest grade students
        System.out.println("\n=== Top 2 students ===");
        students.stream()
                .sorted((s1, s2) -> s2.getGrade() - s1.getGrade())  // Descending
                .limit(2)
                .forEach(s -> System.out.println(s.getName() + ": " + s.getGrade()));

        // QUESTION 4: Group students by department
        System.out.println("\n=== Students by department ===");
        Map<String, List<Student>> byDept = students.stream()
                .collect(Collectors.groupingBy(Student::getDepartment));

        byDept.forEach((dept, list) ->
                System.out.println(dept + ": " + list.size() + " students"));
    }
}
