package ir.dotin.exam.test.sample;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(
            UserRepository repository) {

        this.repository = repository;
    }

    public User getUser(Long id) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new UserNotFoundException(id)
                );
    }

    public User createUser(String name) {

        User user =
                new User(name);

        return repository.save(user);
    }

    public void deleteUser(Long id) {

        repository.deleteById(id);
    }
}
