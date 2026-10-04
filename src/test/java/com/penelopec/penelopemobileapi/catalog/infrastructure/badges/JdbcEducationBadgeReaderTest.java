package com.penelopec.penelopemobileapi.catalog.infrastructure.badges;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penelopec.penelopemobileapi.catalog.application.EducationBadgeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcEducationBadgeReaderTest {
  private JdbcTemplate jdbc;
  private JdbcEducationBadgeReader reader;

  @BeforeEach
  void setUp() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(
      "jdbc:h2:mem:badge_reader_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
    jdbc = new JdbcTemplate(dataSource);
    jdbc.execute("DROP TABLE IF EXISTS property_badge_score");
    jdbc.execute("""
      CREATE TABLE property_badge_score (
        property_id BIGINT, badge_type VARCHAR(30), score DECIMAL(5,2),
        granted BOOLEAN, calculation_status VARCHAR(40), radius_meters INT,
        rule_version VARCHAR(50), source_run_id BIGINT, evidence VARCHAR(4000),
        calculated_at TIMESTAMP
      )
      """);
    reader = new JdbcEducationBadgeReader(dataSource, new ObjectMapper());
  }

  @Test
  void mapsCalculatedZeroAndKeepsTheEstateIdentifierDistinctFromAdvertisementIdentifier() {
    jdbc.update("""
      INSERT INTO property_badge_score VALUES
      (42, 'EDUCATION', 0.00, FALSE, 'CALCULATED', 3000, 'education-v1', 7,
       '{"schools_within_radius":0,"nearest_meters":null,"distance_method":"STRAIGHT_LINE_HAVERSINE",'
       || '"school_source":"INEP_SCHOOLS","school_source_loaded_at":"2026-10-02T09:00:00"}',
       TIMESTAMP '2026-10-02 10:00:00')
      """);

    EducationBadgeResponse result = reader.findByEstateId(42L).orElseThrow();

    assertThat(result.status()).isEqualTo("CALCULATED");
    assertThat(result.score()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(result.granted()).isFalse();
    assertThat(result.schoolsWithinRadius()).isZero();
    assertThat(result.nearestMeters()).isNull();
    assertThat(result.sourceRunId()).isEqualTo(7L);
    assertThat(reader.findByEstateId(10L).orElseThrow().status()).isEqualTo("NOT_CALCULATED");
  }

  @Test
  void keepsMissingCoordinatesAsNullScore() {
    jdbc.update("""
      INSERT INTO property_badge_score VALUES
      (50, 'EDUCATION', NULL, FALSE, 'NO_COORDINATES', 3000, 'education-v1', 7,
       '{"school_source":"INEP_SCHOOLS","school_source_loaded_at":"2026-10-02T09:00:00"}',
       TIMESTAMP '2026-10-02 10:00:00')
      """);

    EducationBadgeResponse result = reader.findByEstateId(50L).orElseThrow();
    assertThat(result.status()).isEqualTo("NO_COORDINATES");
    assertThat(result.score()).isNull();
    assertThat(result.schoolsWithinRadius()).isNull();
    assertThat(result.granted()).isFalse();
  }
}
