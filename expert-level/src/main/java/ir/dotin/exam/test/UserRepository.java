package ir.dotin.exam.test;

public interface UserRepository {

    User findById(Long id);

    User save(User user);

    void deleteById(Long id);
}
