package com.penelopec.penelopemobileapi.auth.application;

public record LoginResponse(String token, Long id, String accessLevel) {
}