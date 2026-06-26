package ir.dotin.exam.test.sample;


import jakarta.validation.constraints.NotBlank;

public class CreateUserRequest {

    @NotBlank
    private String name;

    public CreateUserRequest() {
    }

    public CreateUserRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
