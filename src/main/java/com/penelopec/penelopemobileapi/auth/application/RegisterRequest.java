package com.penelopec.penelopemobileapi.auth.application;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
  @NotBlank String name,
  @NotNull LocalDate birthDate,
  @Email @NotBlank String email,
  @NotBlank @Size(min = 6) String password
) {
}