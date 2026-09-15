package com.penelopec.penelopemobileapi.shared.web.error;

import com.penelopec.penelopemobileapi.shared.core.exception.CoreErrorCode;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
  Instant timestamp,
  int status,
  String code,
  String message,
  String path,
  List<ApiValidationViolation> violations
) {
  public static ApiErrorResponse of(int status, String code, String message, String path) {
    return new ApiErrorResponse(Instant.now(), status, code, message, path, List.of());
  }

  public static ApiErrorResponse validation(String path, List<ApiValidationViolation> violations) {
    return new ApiErrorResponse(
      Instant.now(),
      400,
      CoreErrorCode.VALIDATION_FAILED.code(),
      CoreErrorCode.VALIDATION_FAILED.defaultMessage(),
      path,
      List.copyOf(violations)
    );
  }
}
