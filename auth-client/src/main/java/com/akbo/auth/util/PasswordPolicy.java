package com.akbo.auth.util;

import com.akbo.auth.exception.BadRequestException;

/** Validation rules shared by all flows that accept a new password. */
public final class PasswordPolicy {

    public static final int MINIMUM_LENGTH = 12;
    private static final String ERROR_MESSAGE =
            "Password must be at least 12 characters and contain an uppercase letter, "
                    + "a lowercase letter, a digit, and a special character.";

    private PasswordPolicy() {
        // Utility class.
    }

    public static void validate(final String password) {
        if (password == null
                || password.length() < MINIMUM_LENGTH
                || password.chars().anyMatch(Character::isWhitespace)
                || password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isLowerCase)
                || password.chars().noneMatch(Character::isDigit)
                || password.chars().allMatch(Character::isLetterOrDigit)) {
            throw new BadRequestException(ERROR_MESSAGE);
        }
    }
}
