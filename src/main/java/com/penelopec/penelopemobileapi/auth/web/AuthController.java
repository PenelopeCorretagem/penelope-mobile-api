package com.penelopec.penelopemobileapi.auth.web;

import com.penelopec.penelopemobileapi.auth.application.AuthService;
import com.penelopec.penelopemobileapi.auth.application.LoginRequest;
import com.penelopec.penelopemobileapi.auth.application.LoginResponse;
import com.penelopec.penelopemobileapi.auth.application.RegisterRequest;
import com.penelopec.penelopemobileapi.auth.application.RegisterResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private static final String MESSAGE_KEY = "message";

  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
  }

  @PostMapping("/validate-access-token")
  public ResponseEntity<ValidateAccessTokenResponse> validateAccessToken(
    @Valid @RequestBody TokenRequest request) {
    return authService.validateAccessToken(request.token())
      .map(identity -> ResponseEntity.ok(
        new ValidateAccessTokenResponse(identity.email(), identity.accessLevel().name())
      ))
      .orElseGet(() -> ResponseEntity.ok(new ValidateAccessTokenResponse(null, null)));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody EmailRequest request) {
    authService.requestPasswordReset(request.email());
    return ResponseEntity.ok(Map.of(
      MESSAGE_KEY, "Se o e-mail estiver cadastrado, um código de verificação será enviado."
    ));
  }

  @PostMapping("/validate-reset-token")
  public ResponseEntity<Map<String, String>> validateResetToken(@Valid @RequestBody TokenRequest request) {
    authService.validateResetToken(request.token());
    return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Token válido."));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request.token(), request.newPassword());
    return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Senha redefinida com sucesso."));
  }

  public record TokenRequest(@NotBlank String token) { }
  public record EmailRequest(@Email @NotBlank String email) { }
  public record ResetPasswordRequest(@NotBlank String token, @NotBlank String newPassword) { }
  public record ValidateAccessTokenResponse(String email, String accessLevel) { }
}
