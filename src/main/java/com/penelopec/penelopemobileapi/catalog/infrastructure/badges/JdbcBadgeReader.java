package com.penelopec.penelopemobileapi.catalog.infrastructure.badges;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.penelopec.penelopemobileapi.catalog.application.BadgeReader;
import com.penelopec.penelopemobileapi.catalog.application.BadgeRecord;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class JdbcBadgeReader implements BadgeReader, AutoCloseable {
  private static final String FIND_BADGES = """
    SELECT badge_type, score_10, granted, calculation_status, radius_meters,
           rule_version, evidence, calculated_at
    FROM property_badge_score
    WHERE property_id = ?
      AND badge_type IN ('EDUCATION', 'HEALTH_PRIMARY_CARE', 'HEALTH_GENERAL_HOSPITAL',
                         'GREEN_PARK_NEARBY', 'TRANSPORT_ACCESS', 'SAFETY_OCCURRENCES')
    """;
  private static final TypeReference<Map<String, Object>> EVIDENCE_TYPE = new TypeReference<>() {
  };

  private final DataSource dataSource;
  private final JdbcTemplate jdbc;
  private final ObjectMapper json;

  public JdbcBadgeReader(DataSource dataSource, ObjectMapper json) {
    this.dataSource = dataSource;
    this.jdbc = new JdbcTemplate(dataSource);
    this.json = json;
  }

  @Override
  public List<BadgeRecord> findByEstateId(Long estateId) {
    return jdbc.query(FIND_BADGES, this::toRecord, estateId);
  }

  private BadgeRecord toRecord(ResultSet row, int rowNumber) throws SQLException {
    Timestamp calculatedAt = row.getTimestamp("calculated_at");
    return new BadgeRecord(
      row.getString("badge_type"),
      row.getBigDecimal("score_10"),
      row.getBoolean("granted"),
      row.getString("calculation_status"),
      row.getObject("radius_meters", Integer.class),
      row.getString("rule_version"),
      calculatedAt == null ? null : calculatedAt.toLocalDateTime(),
      parseEvidence(row.getString("evidence"))
    );
  }

  private Map<String, Object> parseEvidence(String value) {
    if (value == null || value.isBlank()) {
      return Map.of();
    }
    try {
      return json.readValue(value, EVIDENCE_TYPE);
    } catch (JsonProcessingException error) {
      throw new DataRetrievalFailureException("Evidência da insígnia inválida", error);
    }
  }

  @Override
  public void close() {
    if (dataSource instanceof HikariDataSource hikari) {
      hikari.close();
    }
  }
}
