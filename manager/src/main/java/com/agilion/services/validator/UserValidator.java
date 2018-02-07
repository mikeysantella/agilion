package com.agilion.services.validator;

import com.agilion.domain.app.User;
import com.agilion.services.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * This validator is intended to check for complex validation requirements (ex: two fields must be the same, or
 * a username is a duplicate of an existing user). Simple validation requirements (ex: "username" cannot be null) should
 * be handled on the class itself as an annotation (ex: @NotNull)
 */
@Component
public class UserValidator implements Validator {

    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";
    private static final String CONFIRM_PASSWORD_FIELD = "confirmPassword";

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Autowired
    UserRepository userRepository;

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User)target;

        validateUsername(errors, user.getUsername());
        validatePasswords(errors, user.getPassword(), user.getConfirmPassword());
    }


    private void validateUsername(Errors errors, String username)
    {
        User existingUser = this.userRepository.findByUsername(username);
        if (existingUser != null)
            errors.rejectValue(USERNAME_FIELD, "validation.user.username.duplicate");
    }

    private void validatePasswords(Errors errors, String password, String confirmPassword)
    {
        if (!password.equals(confirmPassword))
            errors.rejectValue(CONFIRM_PASSWORD_FIELD, "validation.user.password.mismatch");
    }

}
