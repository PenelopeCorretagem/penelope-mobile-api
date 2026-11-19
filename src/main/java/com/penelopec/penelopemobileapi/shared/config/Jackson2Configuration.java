package com.penelopec.penelopemobileapi.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Spring Boot 4 autoconfigura Jackson 3; componentes que usam Jackson 2 precisam deste bean.
@Configuration
public class Jackson2Configuration {

  @Bean
  @ConditionalOnMissingBean(ObjectMapper.class)
  ObjectMapper jackson2ObjectMapper() {
    return new ObjectMapper();
  }
}
