package ir.dotin.exam;

import ir.dotin.exam.test.Calculator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class SpyVsMockTest {

    @Test
    void testMock() {
        System.out.println("\n=== MOCK TEST ===");

        // Create a mock
        Calculator mockCalc = Mockito.mock(Calculator.class);

        // What happens when we call methods?
        System.out.println("Calling mock.add(2,3)");
        int result1 = mockCalc.add(2, 3);
        System.out.println("Result: " + result1);
        System.out.println("Real method NOT called - returns default 0");

        System.out.println("\nCalling mock.getCallCount()");
        int count = mockCalc.getCallCount();
        System.out.println("Result: " + count);
        System.out.println("Returns default 0 - real method NOT called");

        // We can stub it
        when(mockCalc.add(2, 3)).thenReturn(999);
        System.out.println("\nAfter stubbing: mockCalc.add(2,3) = " + mockCalc.add(2, 3));
        System.out.println("Stubbed value overrides everything");
    }

    @Test
    void testSpy() {
        System.out.println("\n=== SPY TEST ===");

        // Create a REAL object
        Calculator realCalc = new Calculator();
        // Create a spy that wraps the real object
        Calculator spyCalc = spy(realCalc);

        System.out.println("Calling spyCalc.add(2,3)");
        int result1 = spyCalc.add(2, 3);
        System.out.println("Result: " + result1);
        System.out.println("Real method WAS called!");

        System.out.println("\nCalling spyCalc.getCallCount()");
        int count = spyCalc.getCallCount();
        System.out.println("Result: " + count);
        System.out.println("Real method WAS called - count incremented!");

        // Stubbing a spy
        System.out.println("\n--- Now stubbing ---");
        when(spyCalc.add(2, 3)).thenReturn(888);
        System.out.println("After stubbing: spyCalc.add(2,3) = " + spyCalc.add(2, 3));
        System.out.println("But real method is STILL called! (check console above)");

        // Important: For spies, use doReturn to avoid calling real method
        System.out.println("\n--- Using doReturn (safe) ---");
        doReturn(777).when(spyCalc).add(5, 5);
        System.out.println("doReturn: spyCalc.add(5,5) = " + spyCalc.add(5, 5));
        System.out.println("Real method NOT called this time!");
    }

    @Test
    void testTheCriticalDifference() {
        System.out.println("\n=== CRITICAL DIFFERENCE ===");

        // MOCK - no real behavior
        Calculator mockCalc = mock(Calculator.class);
        mockCalc.add(1, 2);
        mockCalc.add(3, 4);
        System.out.println("Mock callCount: " + mockCalc.getCallCount()); // 0

        // SPY - tracks real behavior
        Calculator spyCalc = spy(new Calculator());
        spyCalc.add(1, 2);
        spyCalc.add(3, 4);
        System.out.println("Spy callCount: " + spyCalc.getCallCount()); // 2
    }
}