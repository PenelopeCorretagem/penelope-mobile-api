package com.penelopec.penelopemobileapi.market.infrastructure;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MarketTrendConfiguration {
  @Bean(destroyMethod = "close")
  @ConditionalOnProperty(prefix = "app.trends", name = "enabled", havingValue = "true")
  JdbcMarketTrendReader jdbcMarketTrendReader(
    @Value("${app.trends.datasource.url}") String url,
    @Value("${app.trends.datasource.username}") String username,
    @Value("${app.trends.datasource.password}") String password
  ) {
    if (url.isBlank() || username.isBlank()) {
      throw new IllegalStateException("TRENDS_DB_URL e TRENDS_DB_USER são obrigatórios quando TRENDS_ENABLED=true");
    }
    var config = new HikariConfig();
    config.setPoolName("penelope-itbi-readonly");
    config.setJdbcUrl(url);
    config.setUsername(username);
    config.setPassword(password);
    config.setReadOnly(true);
    config.setInitializationFailTimeout(-1);
    config.setConnectionTimeout(2000);
    config.setMinimumIdle(0);
    config.setMaximumPoolSize(3);
    // O pool analítico fica encapsulado para preservar o DataSource principal do Spring.
    return new JdbcMarketTrendReader(new HikariDataSource(config));
  }
}
