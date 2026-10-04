package com.penelopec.penelopemobileapi.catalog.infrastructure.badges;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "TEST_BADGES_DB_URL", matches = ".+")
class JdbcEducationBadgeMysqlSmokeTest {
  @Test
  void readsRealMysqlScoreAndMissingDataStatesWithReadOnlyUser() {
    DriverManagerDataSource source = new DriverManagerDataSource(
      System.getenv("TEST_BADGES_DB_URL"),
      System.getenv("TEST_BADGES_DB_USER"),
      System.getenv("TEST_BADGES_DB_PASSWORD")
    );
    JdbcEducationBadgeReader reader = new JdbcEducationBadgeReader(source, new ObjectMapper());

    var granted = reader.findByEstateId(900000000001L).orElseThrow();
    assertThat(granted.status()).isEqualTo("CALCULATED");
    assertThat(granted.score()).isEqualByComparingTo(new BigDecimal("100.00"));
    assertThat(granted.granted()).isTrue();
    assertThat(granted.schoolsWithinRadius()).isEqualTo(67);
    assertThat(granted.source()).isEqualTo("INEP_SCHOOLS");

    assertThat(reader.findByEstateId(900000000002L).orElseThrow().status())
      .isEqualTo("OUT_OF_COVERAGE");
    assertThat(reader.findByEstateId(900000000003L).orElseThrow().score()).isNull();
    assertThat(reader.findByEstateId(900000000004L).orElseThrow().status())
      .isEqualTo("LOW_PRECISION");
  }
}
