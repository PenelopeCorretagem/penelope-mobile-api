package com.penelopec.penelopemobileapi.auth.domain;

import com.penelopec.penelopemobileapi.shared.core.exception.ValidationException;

import java.time.Instant;
import java.time.LocalDate;

public class User {

  private final Long id;
  private String name;
  private String email;
  private LocalDate birthDate;
  private String password;
  private final AccessLevel accessLevel;
  private String passwordResetToken;
  private Instant passwordResetTokenExpiry;

  private User(Long id, String name, String email, LocalDate birthDate, String password, AccessLevel accessLevel,
               String passwordResetToken, Instant passwordResetTokenExpiry) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.birthDate = birthDate;
    this.password = password;
    this.accessLevel = accessLevel;
    this.passwordResetToken = passwordResetToken;
    this.passwordResetTokenExpiry = passwordResetTokenExpiry;
  }

  public static User restore(Long id, String email, String password, AccessLevel accessLevel,
                             String passwordResetToken, Instant passwordResetTokenExpiry) {
    return restore(id, null, email, null, password, accessLevel, passwordResetToken, passwordResetTokenExpiry);
  }

  public static User restore(Long id, String name, String email, LocalDate birthDate, String password,
                             AccessLevel accessLevel, String passwordResetToken, Instant passwordResetTokenExpiry) {
    return new User(id, name, email, birthDate, password, accessLevel, passwordResetToken, passwordResetTokenExpiry);
  }

  public void generatePasswordResetToken(String token, Instant expiry) {
    passwordResetToken = token;
    passwordResetTokenExpiry = expiry;
  }

  public void applyNewPassword(String encryptedPassword, Instant now) {
    if (passwordResetTokenExpiry == null || !passwordResetTokenExpiry.isAfter(now)) {
      throw new ValidationException(AuthErrorCode.EXPIRED_RESET_TOKEN.toError());
    }
    password = encryptedPassword;
    passwordResetToken = null;
    passwordResetTokenExpiry = null;
  }

  public void updateProfile(String name, String email, LocalDate birthDate) {
    this.name = name;
    this.email = email;
    this.birthDate = birthDate;
  }

  public Long getId() { return id; }
  public String getName() { return name; }
  public String getEmail() { return email; }
  public LocalDate getBirthDate() { return birthDate; }
  public String getPassword() { return password; }
  public AccessLevel getAccessLevel() { return accessLevel; }
  public String getPasswordResetToken() { return passwordResetToken; }
  public Instant getPasswordResetTokenExpiry() { return passwordResetTokenExpiry; }
}
