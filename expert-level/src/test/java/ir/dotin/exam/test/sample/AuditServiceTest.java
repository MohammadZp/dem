package ir.dotin.exam.test.sample;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AuditServiceTest {

    @Test
    void spyExample() {

        // real object but wrapped
        AuditService audit = spy(new AuditService());

        // override only this method
        doNothing()
                .when(audit)
                .writeToFile(anyString());

        // act
        audit.audit("Created User");

        // assert (verify interaction)
        verify(audit).writeToFile("Created User");
    }
}
