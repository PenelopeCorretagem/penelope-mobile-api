package com.penelopec.penelopemobileapi.auth.application;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateUserProfileRequest(
  @NotBlank String name,
  @Email @NotBlank String email,
  LocalDate birthDate,
  @Size(max = 8_000_000) String profileImage
) {
  public UpdateUserProfileRequest(String name, String email, LocalDate birthDate) {
    this(name, email, birthDate, null);
  }
}