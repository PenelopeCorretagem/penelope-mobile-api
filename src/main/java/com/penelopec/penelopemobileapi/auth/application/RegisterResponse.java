package com.penelopec.penelopemobileapi.auth.application;

import java.time.LocalDate;

public record RegisterResponse(Long id, String name, String email, LocalDate birthDate, String accessLevel) {
}