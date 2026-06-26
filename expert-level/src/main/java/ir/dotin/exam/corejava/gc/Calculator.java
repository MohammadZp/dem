package ir.dotin.exam.corejava.gc;

@FunctionalInterface
public interface Calculator<R> {

    R calculate(R r1, R r2, R r3);
}


