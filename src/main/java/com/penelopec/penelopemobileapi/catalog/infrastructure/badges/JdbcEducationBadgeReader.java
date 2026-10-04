package com.penelopec.penelopemobileapi.catalog.infrastructure.badges;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.penelopec.penelopemobileapi.catalog.application.EducationBadgeReader;
import com.penelopec.penelopemobileapi.catalog.application.EducationBadgeResponse;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class JdbcEducationBadgeReader implements EducationBadgeReader, AutoCloseable {
  private static final String FIND_SCORE = """
    SELECT score, granted, calculation_status, radius_meters, rule_version,
           source_run_id, evidence, calculated_at
    FROM property_badge_score
    WHERE property_id = ? AND badge_type = 'EDUCATION'
    """;

  private final DataSource dataSource;
  private final JdbcTemplate jdbc;
  private final ObjectMapper json;

  public JdbcEducationBadgeReader(DataSource dataSource, ObjectMapper json) {
    this.dataSource = dataSource;
    this.jdbc = new JdbcTemplate(dataSource);
    this.json = json;
  }

  @Override
  public Optional<EducationBadgeResponse> findByEstateId(Long estateId) {
    List<EducationBadgeResponse> results = jdbc.query(FIND_SCORE, this::toResponse, estateId);
    return Optional.of(results.isEmpty() ? EducationBadgeResponse.notCalculated() : results.getFirst());
  }

  private EducationBadgeResponse toResponse(ResultSet row, int rowNumber) throws SQLException {
    JsonNode evidence = parseEvidence(row.getString("evidence"));
    return new EducationBadgeResponse(
      row.getString("calculation_status"),
      row.getBigDecimal("score"),
      row.getBoolean("granted"),
      row.getInt("radius_meters"),
      integer(evidence, "schools_within_radius"),
      decimal(evidence, "nearest_meters"),
      string(evidence, "distance_method"),
      row.getString("rule_version"),
      string(evidence, "school_source"),
      row.getObject("source_run_id", Long.class),
      dateTime(evidence, "school_source_loaded_at"),
      timestamp(row.getTimestamp("calculated_at"))
    );
  }

  private JsonNode parseEvidence(String value) {
    try {
      return json.readTree(value);
    } catch (JsonProcessingException error) {
      throw new DataRetrievalFailureException("Evidência da insígnia de educação inválida", error);
    }
  }

  private static String string(JsonNode root, String field) {
    JsonNode value = root.path(field);
    return value.isMissingNode() || value.isNull() ? null : value.asText();
  }

  private static Integer integer(JsonNode root, String field) {
    JsonNode value = root.path(field);
    return value.isNumber() ? value.intValue() : null;
  }

  private static BigDecimal decimal(JsonNode root, String field) {
    JsonNode value = root.path(field);
    return value.isNumber() ? value.decimalValue() : null;
  }

  private static LocalDateTime dateTime(JsonNode root, String field) {
    String value = string(root, field);
    if (value == null) {
      return null;
    }
    try {
      return LocalDateTime.parse(value);
    } catch (DateTimeParseException error) {
      throw new DataRetrievalFailureException("Data da fonte da insígnia de educação inválida", error);
    }
  }

  private static LocalDateTime timestamp(Timestamp value) {
    return value == null ? null : value.toLocalDateTime();
  }

  @Override
  public void close() throws Exception {
    if (dataSource instanceof HikariDataSource hikari) {
      hikari.close();
    }
  }
}
