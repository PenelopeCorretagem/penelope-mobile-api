package com.penelopec.penelopemobileapi.auth.domain;

public interface PasswordResetNotifier {
  void send(String email, String token);
}