package ir.dotin.exam.corejava;

public class Student implements Comparable<Student> {
    String name;
    int grade;

    Student(String name, int grade) {
        this.name = name;
        this.grade = grade;
    }

    @Override
    public int compareTo(Student o) {
        if (o.grade == this.grade) {
            return this.name.compareTo(o.name);
        } else if (this.grade > o.grade) {
            return 1;
        } else {
            return -1;
        }

    }
}