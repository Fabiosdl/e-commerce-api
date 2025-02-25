package com.fabiolima.e_commerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class RegistrationRequest {

    @NotBlank
    @Size(min = 2, max = 40, message = "Name has to have a minimum of 2 characters and a maximum of 40.")
    private String name;

    @NotBlank(message = "Username is your email address and it should not be blank.")
    @Email(regexp = "[a-z0-9._%-+]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Invalid email format.")
    private String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must have at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character.")
    @NotBlank
    private String password;

    ///Role is being added in AuthenticationServiceImpl.registerUser(RegistrationRequest registrationInput)
}