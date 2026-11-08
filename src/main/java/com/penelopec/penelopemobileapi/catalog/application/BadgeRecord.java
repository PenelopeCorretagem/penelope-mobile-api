package com.penelopec.penelopemobileapi.catalog.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/** Linha bruta de property_badge_score, ainda sem regras de apresentação. */
public record BadgeRecord(
  String type,
  BigDecimal score10,
  boolean granted,
  String calculationStatus,
  Integer radiusMeters,
  String ruleVersion,
  LocalDateTime calculatedAt,
  Map<String, Object> evidence
) {
}
