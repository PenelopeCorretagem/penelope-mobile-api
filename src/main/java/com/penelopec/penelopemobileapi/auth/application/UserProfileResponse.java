package com.penelopec.penelopemobileapi.auth.application;

import java.time.LocalDate;

public record UserProfileResponse(String name, String email, LocalDate birthDate) {
}