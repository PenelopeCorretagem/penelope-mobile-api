package com.penelopec.penelopemobileapi.auth.application;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdateUserProfileRequest(
  @NotBlank String name,
  @Email @NotBlank String email,
  LocalDate birthDate
) {
}