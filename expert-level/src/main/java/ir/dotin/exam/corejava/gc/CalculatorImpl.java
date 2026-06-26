package ir.dotin.exam.corejava.gc;

public class CalculatorImpl implements Calculator<Integer>{



    @Override
    public Integer calculate(Integer r1, Integer r2, Integer r3) {
        return r1+r2+r3;
    }
}
