package com.penelopec.penelopemobileapi.catalog.infrastructure.badges;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penelopec.penelopemobileapi.catalog.application.BadgeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcBadgeReaderTest {
  private JdbcTemplate jdbc;
  private JdbcBadgeReader reader;

  @BeforeEach
  void setUp() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(
      "jdbc:h2:mem:badge_reader_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
    jdbc = new JdbcTemplate(dataSource);
    jdbc.execute("DROP TABLE IF EXISTS property_badge_score");
    jdbc.execute("""
      CREATE TABLE property_badge_score (
        property_id BIGINT, badge_type VARCHAR(30), score_10 DECIMAL(3,1),
        granted BOOLEAN, calculation_status VARCHAR(40), radius_meters INT,
        rule_version VARCHAR(50), evidence VARCHAR(4000), calculated_at TIMESTAMP
      )
      """);
    reader = new JdbcBadgeReader(dataSource, new ObjectMapper());
  }

  @Test
  void readsOnlyTheRequestedEstateWithScoreAndEvidence() {
    jdbc.update("""
      INSERT INTO property_badge_score VALUES
      (42, 'TRANSPORT_ACCESS', 6.8, TRUE, 'CALCULATED', 1500, 'transport-access-v1',
       '{"rail_nearest_meters":300.0,"bus_stops_within_radius":4}', TIMESTAMP '2026-10-02 10:00:00'),
      (42, 'GREEN_PARK_NEARBY', NULL, FALSE, 'NO_COORDINATES', 1000, 'green-proximity-v3',
       '{}', TIMESTAMP '2026-10-02 10:00:00'),
      (42, 'LEGACY_UNKNOWN', 1.0, FALSE, 'CALCULATED', 1, 'x', '{}', TIMESTAMP '2026-10-02 10:00:00'),
      (43, 'EDUCATION', 9.0, TRUE, 'CALCULATED', 3000, 'education-v1', '{}', TIMESTAMP '2026-10-02 10:00:00')
      """);

    List<BadgeRecord> result = reader.findByEstateId(42L);

    assertThat(result).extracting(BadgeRecord::type)
      .containsExactlyInAnyOrder("TRANSPORT_ACCESS", "GREEN_PARK_NEARBY");
    BadgeRecord transport = result.stream().filter(item -> item.type().equals("TRANSPORT_ACCESS")).findFirst().orElseThrow();
    assertThat(transport.score10()).isEqualByComparingTo("6.8");
    assertThat(transport.granted()).isTrue();
    assertThat(transport.evidence()).containsEntry("bus_stops_within_radius", 4);
    BadgeRecord green = result.stream().filter(item -> item.type().equals("GREEN_PARK_NEARBY")).findFirst().orElseThrow();
    assertThat(green.score10()).isNull();
    assertThat(reader.findByEstateId(10L)).isEmpty();
  }
}
