package com.penelopec.penelopemobileapi.auth.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AuthConfiguration {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
