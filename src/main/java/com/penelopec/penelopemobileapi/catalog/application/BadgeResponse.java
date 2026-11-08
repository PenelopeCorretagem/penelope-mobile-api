package com.penelopec.penelopemobileapi.catalog.application;

import java.math.BigDecimal;
import java.util.Map;

public record BadgeResponse(
  String type,
  String label,
  BigDecimal score,
  boolean granted,
  String status,
  String reason,
  String explanation,
  Map<String, Object> evidence
) {
}
