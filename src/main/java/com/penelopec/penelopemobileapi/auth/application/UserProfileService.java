package com.penelopec.penelopemobileapi.auth.application;

import com.penelopec.penelopemobileapi.auth.domain.AuthErrorCode;
import com.penelopec.penelopemobileapi.auth.domain.TokenService;
import com.penelopec.penelopemobileapi.auth.domain.User;
import com.penelopec.penelopemobileapi.auth.domain.UserRepository;
import com.penelopec.penelopemobileapi.shared.core.exception.NotFoundException;
import com.penelopec.penelopemobileapi.shared.core.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {
  private final UserRepository users;
  private final TokenService tokens;

  @Transactional(readOnly = true)
  public UserProfileResponse getProfile(String email) {
    return toResponse(findUser(email));
  }

  @Transactional
  public UpdateUserProfileResponse updateProfile(String currentEmail, UpdateUserProfileRequest request) {
    User user = findUser(currentEmail);
    boolean emailChanged = !user.getEmail().equalsIgnoreCase(request.email());
    if (emailChanged) {
      ensureEmailIsAvailable(request.email());
    }

    user.updateProfile(request.name(), request.email(), request.birthDate());
    User savedUser = users.save(user);
    String token = emailChanged ? tokens.generate(savedUser.getEmail(), savedUser.getAccessLevel()) : null;
    return new UpdateUserProfileResponse(toResponse(savedUser), token);
  }

  private User findUser(String email) {
    return users.findByEmail(email)
      .orElseThrow(() -> new NotFoundException(AuthErrorCode.USER_NOT_FOUND.toError()));
  }

  private void ensureEmailIsAvailable(String email) {
    if (users.findByEmail(email).isPresent()) {
      throw new ValidationException(AuthErrorCode.EMAIL_ALREADY_IN_USE.toError());
    }
  }

  private UserProfileResponse toResponse(User user) {
    return new UserProfileResponse(user.getName(), user.getEmail(), user.getBirthDate());
  }
}