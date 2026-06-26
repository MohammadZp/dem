package ir.dotin.exam.corejava.gc;

import java.io.Serializable;

public class Main {
    public static void main(String[] args) {
        Calculator<Integer> multiplier = (r1, r2, r3) -> r1 * r2 * r3;
        Calculator<Long> summation = (r1, r2, r3) -> r1 + r2 + r3;

        process(multiplier,2,4,5);
        process(summation, 2L, 4L, 5L);
    }


    static <T extends Number & Serializable> void process(Calculator<T> calculator, T a, T b, T c) {
        T result = calculator.calculate(a, b, c);
        System.out.println("Result: " + result + " (type: " + result.getClass().getSimpleName() + ")");
    }
}
