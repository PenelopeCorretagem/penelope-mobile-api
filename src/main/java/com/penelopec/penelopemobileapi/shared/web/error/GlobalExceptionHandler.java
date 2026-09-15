package com.penelopec.penelopemobileapi.shared.web.error;

import com.penelopec.penelopemobileapi.shared.core.exception.BusinessException;
import com.penelopec.penelopemobileapi.shared.core.exception.CoreErrorCode;
import com.penelopec.penelopemobileapi.shared.core.exception.CoreException;
import com.penelopec.penelopemobileapi.shared.core.exception.NotFoundException;
import com.penelopec.penelopemobileapi.shared.core.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(CoreException.class)
  public ResponseEntity<ApiErrorResponse> handleCoreException(
    CoreException exception,
    HttpServletRequest request) {
    HttpStatus status = resolveStatus(exception);
    var error = exception.domainError();

    return ResponseEntity.status(status)
      .body(ApiErrorResponse.of(status.value(), error.code(), error.message(), request.getRequestURI()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
    MethodArgumentNotValidException exception,
    HttpServletRequest request) {
    List<ApiValidationViolation> violations = exception.getBindingResult().getFieldErrors().stream()
      .map(fieldError -> new ApiValidationViolation(
        fieldError.getField(),
        fieldError.getDefaultMessage(),
        fieldError.getCode()
      ))
      .toList();

    return ResponseEntity.badRequest()
      .body(ApiErrorResponse.validation(request.getRequestURI(), violations));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
    Exception exception,
    HttpServletRequest request) {
    LOGGER.error("Erro inesperado ao processar a requisição {}", request.getRequestURI(), exception);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ApiErrorResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        CoreErrorCode.INTERNAL_ERROR.code(),
        CoreErrorCode.INTERNAL_ERROR.defaultMessage(),
        request.getRequestURI()
      ));
  }

  private HttpStatus resolveStatus(CoreException exception) {
    if (exception instanceof ValidationException) {
      return HttpStatus.BAD_REQUEST;
    }
    if (exception instanceof NotFoundException) {
      return HttpStatus.NOT_FOUND;
    }
    if (exception instanceof BusinessException) {
      return HttpStatus.UNPROCESSABLE_ENTITY;
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
