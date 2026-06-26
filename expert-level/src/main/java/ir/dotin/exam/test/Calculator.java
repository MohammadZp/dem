package ir.dotin.exam.test;

// A simple class with a method that does real work
public class Calculator {
    private int callCount = 0;

    public int add(int a, int b) {
        callCount++;  // Tracks how many times called
        System.out.println("🔴 REAL add() executed with: " + a + " + " + b);
        return a + b;
    }

    public int getCallCount() {
        return callCount;
    }


}
