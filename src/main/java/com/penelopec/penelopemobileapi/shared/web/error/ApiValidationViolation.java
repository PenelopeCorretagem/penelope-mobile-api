package com.penelopec.penelopemobileapi.shared.web.error;

public record ApiValidationViolation(String field, String message, String code) {
}
