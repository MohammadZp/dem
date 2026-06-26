package ir.dotin.exam;

import ir.dotin.exam.test.AuditService;
import ir.dotin.exam.test.User;
import ir.dotin.exam.test.UserRepository;
import ir.dotin.exam.test.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository repository;

    @InjectMocks
    UserService service;

    @Test
    void shouldGetUser() {

        User user =
                new User(1L, "Ali");

        when(
                repository.findById(1L)
        ).thenReturn(user);

        User result =
                service.getUser(1L);

        assertEquals(
                "Ali",
                result.getName()
        );
    }

    @Test
    void shouldCallRepository() {

        when(
                repository.findById(1L)
        ).thenReturn(
                new User(1L, "Ali")
        );

        service.getUser(1L);

        verify(repository)
                .findById(1L);
    }

    @Test
    void shouldCreateUser() {

        User saved =
                new User(1L, "Ali");

        when(
                repository.save(any())
        ).thenReturn(saved);

        User result =
                service.createUser("Ali");

        assertEquals(
                1L,
                result.getId()
        );
    }

    @Test
    void shouldDeleteUser() {

        service.deleteUser(1L);

        verify(repository)
                .deleteById(1L);
    }

    @Test
    void spyExample() {

        AuditService audit =
                spy(new AuditService());

        audit.log("Created User");

        assertEquals(
                1,
                audit.count()
        );
    }
}
