package com.penelopec.penelopemobileapi.auth.web;

import com.penelopec.penelopemobileapi.auth.application.UpdateUserProfileRequest;
import com.penelopec.penelopemobileapi.auth.application.UpdateUserProfileResponse;
import com.penelopec.penelopemobileapi.auth.application.UserProfileResponse;
import com.penelopec.penelopemobileapi.auth.application.UserProfileService;
import com.penelopec.penelopemobileapi.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users/me")
@RequiredArgsConstructor
public class UserProfileController {
  private final UserProfileService userProfileService;

  @GetMapping
  public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal AuthenticatedUser user) {
    return ResponseEntity.ok(userProfileService.getProfile(user.email()));
  }

  @PatchMapping
  public ResponseEntity<UpdateUserProfileResponse> updateProfile(
    @AuthenticationPrincipal AuthenticatedUser user,
    @Valid @RequestBody UpdateUserProfileRequest request) {
    return ResponseEntity.ok(userProfileService.updateProfile(user.email(), request));
  }
}