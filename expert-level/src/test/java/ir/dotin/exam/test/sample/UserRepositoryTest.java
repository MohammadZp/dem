package ir.dotin.exam.test.sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    UserRepository repository;

    @Test
    void findByName() {

        repository.save(
                new User("Mohammad")
        );

        Optional<User> user =
                repository.findByName("Mohammad");

        assertTrue(user.isPresent());
    }
}