package com.penelopec.penelopemobileapi.catalog.infrastructure.badges;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penelopec.penelopemobileapi.catalog.application.BadgeRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.assertj.core.api.Assertions.assertThat;

/** Somente SELECT no banco real; execução depende de configuração explícita. */
@EnabledIfEnvironmentVariable(named="BADGES_MYSQL_SMOKE_URL", matches=".+")
class JdbcBadgeMysqlSmokeTest {
  @Test
  void readsSevenCategoriesFromMigratedMysqlWithExactGradeConversion() {
    var dataSource = new DriverManagerDataSource(System.getenv("BADGES_MYSQL_SMOKE_URL"),
      System.getenv("BADGES_MYSQL_SMOKE_USER"), System.getenv("BADGES_MYSQL_SMOKE_PASSWORD"));
    var reader = new JdbcBadgeReader(dataSource, new ObjectMapper());
    var rows = reader.findByEstateId(Long.valueOf(System.getenv("BADGES_MYSQL_SMOKE_ESTATE_ID")));
    assertThat(rows).extracting(BadgeRecord::type).containsExactlyInAnyOrder(
      "EDUCATION", "HEALTH_PRIMARY_CARE", "HEALTH_GENERAL_HOSPITAL", "GREEN_PARK_NEARBY",
      "TRANSPORT_BUS_NEARBY", "TRANSPORT_RAIL_NEARBY", "SAFETY_OCCURRENCES");
    assertThat(rows).allSatisfy(row -> {
      assertThat(row.sourceRunId()).isNotNull();
      assertThat(row.calculationStatus()).isEqualTo("CALCULATED");
      assertThat(row.score100()).isNotNull();
      assertThat(row.score10()).isEqualByComparingTo(
        row.score100().divide(BigDecimal.TEN, 1, RoundingMode.HALF_UP));
    });
  }
}
