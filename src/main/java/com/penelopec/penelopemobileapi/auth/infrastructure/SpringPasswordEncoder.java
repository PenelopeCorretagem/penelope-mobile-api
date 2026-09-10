package com.penelopec.penelopemobileapi.auth.infrastructure;

import com.penelopec.penelopemobileapi.auth.domain.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringPasswordEncoder implements PasswordEncoder {
  private final org.springframework.security.crypto.password.PasswordEncoder delegate;

  @Override
  public String encode(String rawPassword) {
    return delegate.encode(rawPassword);
  }

  @Override
  public boolean matches(String rawPassword, String encryptedPassword) {
    return delegate.matches(rawPassword, encryptedPassword);
  }
}
