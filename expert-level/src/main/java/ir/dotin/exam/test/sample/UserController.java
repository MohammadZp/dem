package ir.dotin.exam.test.sample;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(
            UserService service) {

        this.service = service;
    }

    @PostMapping
    public User create(
            @Valid
            @RequestBody
            CreateUserRequest request) {

        return service.createUser(
                request.getName()
        );
    }

    @GetMapping("/{id}")
    public User get(
            @PathVariable Long id) {

        return service.getUser(id);
    }
}
