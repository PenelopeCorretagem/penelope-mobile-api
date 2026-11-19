package com.penelopec.penelopemobileapi.catalog.infrastructure.badges;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penelopec.penelopemobileapi.catalog.application.BadgeReader;
import com.penelopec.penelopemobileapi.catalog.application.BadgeRecord;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BadgeConfiguration {

  @Bean(destroyMethod = "close")
  @ConditionalOnProperty(prefix = "app.badges", name = "enabled", havingValue = "true")
  JdbcBadgeReader jdbcBadgeReader(
    @Value("${app.badges.datasource.url:}") String url,
    @Value("${app.badges.datasource.username:}") String username,
    @Value("${app.badges.datasource.password:}") String password) {
    if (url.isBlank() || username.isBlank()) {
      throw new IllegalStateException("BADGES_DB_URL e BADGES_DB_USER são obrigatórios quando BADGES_ENABLED=true");
    }
    HikariConfig config = new HikariConfig();
    config.setPoolName("penelope-badges-readonly");
    config.setJdbcUrl(url);
    config.setUsername(username);
    config.setPassword(password);
    config.setReadOnly(true);
    config.setInitializationFailTimeout(-1);
    config.setConnectionTimeout(2000);
    config.setMinimumIdle(0);
    config.setMaximumPoolSize(3);
    return new JdbcBadgeReader(new HikariDataSource(config), new ObjectMapper());
  }

  @Bean
  @ConditionalOnProperty(prefix = "app.badges", name = "enabled", havingValue = "false", matchIfMissing = true)
  BadgeReader disabledBadgeReader() {
    return new BadgeReader() {
      @Override
      public List<BadgeRecord> findByEstateId(Long estateId) {
        return List.of();
      }

      @Override
      public boolean isEnabled() {
        return false;
      }
    };
  }
}
