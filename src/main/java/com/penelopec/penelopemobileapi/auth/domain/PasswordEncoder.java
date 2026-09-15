package com.penelopec.penelopemobileapi.auth.domain;

public interface PasswordEncoder {
  String encode(String rawPassword);
  boolean matches(String rawPassword, String encryptedPassword);
}
