package ir.dotin.exam.test.sample;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Test
    void getUser() {

        //arrange
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User(1L, "Mohammad")));
        //act
        User user = userService.getUser(1L);

        //assert
        assertEquals(1L, user.getId());
    }

    @Test
    void getUserThrowException() {

        //arrange
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        //act and assert
        assertThrows(UserNotFoundException.class, () -> userService.getUser(1L));
    }

    @Test
    void createUser() {

        //arrange
        User userTest = new User("Mohammad");
        when(userRepository.save(any()))
                .thenReturn(userTest);
        //act
        User createdUser = userService.createUser("Mohammad");

        //assert
        assertEquals("Mohammad", createdUser.getName());
    }

    @Test
    void deleteUser() {

        //act
        userService.deleteUser(1L);

        //assert
        verify(userRepository)
                .deleteById(1L);
    }


}
