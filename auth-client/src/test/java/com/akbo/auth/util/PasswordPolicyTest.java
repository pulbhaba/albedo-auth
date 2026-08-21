package com.akbo.auth.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.akbo.auth.exception.BadRequestException;
import org.junit.jupiter.api.Test;

class PasswordPolicyTest {

    @Test
    void acceptsPasswordMeetingAllRequirements() {
        assertDoesNotThrow(() -> PasswordPolicy.validate("CorrectHorse1!"));
    }

    @Test
    void rejectsPasswordsMissingStrengthRequirements() {
        assertThrows(BadRequestException.class, () -> PasswordPolicy.validate("short1!"));
        assertThrows(BadRequestException.class, () -> PasswordPolicy.validate("lowercaseonly1!"));
        assertThrows(BadRequestException.class, () -> PasswordPolicy.validate("UPPERCASEONLY1!"));
        assertThrows(BadRequestException.class, () -> PasswordPolicy.validate("NoDigitsHere!!"));
        assertThrows(BadRequestException.class, () -> PasswordPolicy.validate("NoSymbols1234"));
        assertThrows(BadRequestException.class, () -> PasswordPolicy.validate("Contains Space1!"));
    }

    @Test
    void rejectsNullPassword() {
        assertThrows(BadRequestException.class, () -> PasswordPolicy.validate(null));
    }
}
