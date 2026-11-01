package com.penelopec.penelopemobileapi.auth.application;

import com.penelopec.penelopemobileapi.auth.domain.AccessLevel;
import com.penelopec.penelopemobileapi.auth.domain.AuthErrorCode;
import com.penelopec.penelopemobileapi.auth.domain.PasswordEncoder;
import com.penelopec.penelopemobileapi.auth.domain.PasswordResetNotifier;
import com.penelopec.penelopemobileapi.auth.domain.TokenIdentity;
import com.penelopec.penelopemobileapi.auth.domain.TokenService;
import com.penelopec.penelopemobileapi.auth.domain.User;
import com.penelopec.penelopemobileapi.auth.domain.UserRepository;
import com.penelopec.penelopemobileapi.shared.core.exception.BusinessException;
import com.penelopec.penelopemobileapi.shared.core.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository users;
  private final PasswordEncoder passwords;
  private final TokenService tokens;
  private final PasswordResetNotifier notifier;
  private final Clock clock;
  private final SecureRandom secureRandom = new SecureRandom();

  public LoginResponse login(LoginRequest request) {
    User user = users.findByEmail(request.email())
      .orElseThrow(this::invalidCredentials);

    if (!passwords.matches(request.password(), user.getPassword())) {
      throw invalidCredentials();
    }

    String token = tokens.generate(user.getEmail(), user.getAccessLevel());
    return new LoginResponse(token, user.getId(), user.getAccessLevel().name());
  }

  public RegisterResponse register(RegisterRequest request) {
    String email = request.email().trim();
    if (users.findByEmail(email).isPresent()) {
      throw new ValidationException(AuthErrorCode.EMAIL_ALREADY_IN_USE.toError());
    }

    User user = User.restore(null, request.name().trim(), email, request.birthDate(),
      passwords.encode(request.password()), AccessLevel.CLIENTE, null, null);
    User savedUser = users.save(user);
    return new RegisterResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(),
      savedUser.getBirthDate(), savedUser.getAccessLevel().name());
  }

  public Optional<TokenIdentity> validateAccessToken(String token) {
    return tokens.validate(token);
  }

  public void requestPasswordReset(String email) {
    users.findByEmail(email).ifPresent(this::createAndSendPasswordReset);
  }

  public void validateResetToken(String token) {
    User user = users.findByPasswordResetToken(token)
      .orElseThrow(this::invalidResetToken);

    if (user.getPasswordResetTokenExpiry() == null
      || !user.getPasswordResetTokenExpiry().isAfter(clock.instant())) {
      throw new ValidationException(AuthErrorCode.EXPIRED_RESET_TOKEN.toError());
    }
  }

  public void resetPassword(String token, String newPassword) {
    User user = users.findByPasswordResetToken(token)
      .orElseThrow(this::invalidResetToken);

    user.applyNewPassword(passwords.encode(newPassword), clock.instant());
    users.save(user);
  }

  private BusinessException invalidCredentials() {
    return new BusinessException(AuthErrorCode.INVALID_CREDENTIALS.toError());
  }

  private void createAndSendPasswordReset(User user) {
    String token = String.format("%06d", secureRandom.nextInt(1_000_000));
    user.generatePasswordResetToken(token, clock.instant().plus(Duration.ofHours(1)));
    users.save(user);
    notifier.send(user.getEmail(), token);
  }

  private ValidationException invalidResetToken() {
    return new ValidationException(AuthErrorCode.INVALID_TOKEN.toError());
  }
}
