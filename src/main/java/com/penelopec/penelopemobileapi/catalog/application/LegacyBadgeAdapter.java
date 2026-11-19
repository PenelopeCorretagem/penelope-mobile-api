package com.penelopec.penelopemobileapi.catalog.application;

import org.springframework.stereotype.Component;
import org.springframework.dao.DataRetrievalFailureException;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Mantém o contrato do detalhe usando a mesma conexão/leitor do endpoint novo. */
@Component
@RequiredArgsConstructor
public class LegacyBadgeAdapter implements EducationBadgeReader, HealthBadgeReader {
  private final BadgeReader reader;

  @Override
  public Optional<EducationBadgeResponse> findByEstateId(Long estateId) {
    if (!reader.isEnabled()) return Optional.empty();
    return Optional.of(reader.findByEstateId(estateId).stream()
      .filter(r -> r.type().equals("EDUCATION")).findFirst()
      .map(r -> new EducationBadgeResponse(r.calculationStatus(), r.score100(), r.granted(),
        r.radiusMeters(), integer(r.evidence(), "schools_within_radius"),
        decimal(r.evidence(), "nearest_meters"), string(r.evidence(), "distance_method"),
        r.ruleVersion(), string(r.evidence(), "school_source"), r.sourceRunId(),
        date(r.evidence(), "school_source_loaded_at"), r.calculatedAt()))
      .orElseGet(EducationBadgeResponse::notCalculated));
  }

  @Override
  public List<HealthBadgeResponse> findHealthByEstateId(Long estateId) {
    if (!reader.isEnabled()) return List.of();
    List<BadgeRecord> rows = reader.findByEstateId(estateId);
    return List.of(health(rows, "HEALTH_PRIMARY_CARE"), health(rows, "HEALTH_GENERAL_HOSPITAL"));
  }

  private static HealthBadgeResponse health(List<BadgeRecord> rows, String type) {
    return rows.stream().filter(r -> r.type().equals(type)).findFirst()
      .map(r -> new HealthBadgeResponse(type, r.calculationStatus(), r.granted(), r.radiusMeters(),
        integer(r.evidence(), "facility_count_within_radius"), decimal(r.evidence(), "nearest_meters"),
        string(r.evidence(), "distance_method"), r.ruleVersion(), string(r.evidence(), "source"),
        r.sourceRunId(), string(r.evidence(), "source_reference_period"),
        string(r.evidence(), "source_reference_origin"), date(r.evidence(), "source_loaded_at"),
        r.calculatedAt()))
      .orElseGet(() -> HealthBadgeResponse.notCalculated(type));
  }

  private static String string(Map<String, Object> values, String key) {
    Object value = values.get(key);
    return value == null ? null : value.toString();
  }

  private static Integer integer(Map<String, Object> values, String key) {
    return values.get(key) instanceof Number value ? value.intValue() : null;
  }

  private static BigDecimal decimal(Map<String, Object> values, String key) {
    return values.get(key) instanceof Number value ? new BigDecimal(value.toString()) : null;
  }

  private static LocalDateTime date(Map<String, Object> values, String key) {
    String value = string(values, key);
    if (value == null) return null;
    try {
      return LocalDateTime.parse(value);
    } catch (DateTimeParseException error) {
      throw new DataRetrievalFailureException("Data da fonte da insígnia inválida", error);
    }
  }
}
