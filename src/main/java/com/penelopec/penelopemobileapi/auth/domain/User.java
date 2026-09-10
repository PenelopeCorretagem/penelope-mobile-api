package com.penelopec.penelopemobileapi.auth.domain;

import com.penelopec.penelopemobileapi.shared.core.exception.ValidationException;

import java.time.Instant;

public class User {

  private final Long id;
  private final String email;
  private String password;
  private final AccessLevel accessLevel;
  private String passwordResetToken;
  private Instant passwordResetTokenExpiry;

  private User(Long id, String email, String password, AccessLevel accessLevel,
               String passwordResetToken, Instant passwordResetTokenExpiry) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.accessLevel = accessLevel;
    this.passwordResetToken = passwordResetToken;
    this.passwordResetTokenExpiry = passwordResetTokenExpiry;
  }

  public static User restore(Long id, String email, String password, AccessLevel accessLevel,
                             String passwordResetToken, Instant passwordResetTokenExpiry) {
    return new User(id, email, password, accessLevel, passwordResetToken, passwordResetTokenExpiry);
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

  public Long getId() { return id; }
  public String getEmail() { return email; }
  public String getPassword() { return password; }
  public AccessLevel getAccessLevel() { return accessLevel; }
  public String getPasswordResetToken() { return passwordResetToken; }
  public Instant getPasswordResetTokenExpiry() { return passwordResetTokenExpiry; }
}
