package com.penelopec.penelopemobileapi.catalog.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HealthBadgeResponse(
  String type,
  String status,
  boolean granted,
  Integer radiusMeters,
  Integer facilitiesWithinRadius,
  BigDecimal nearestMeters,
  String distanceMethod,
  String ruleVersion,
  String source,
  Long sourceRunId,
  String sourceReferencePeriod,
  String sourceReferenceOrigin,
  LocalDateTime sourceLoadedAt,
  LocalDateTime calculatedAt
) {
  public static HealthBadgeResponse notCalculated(String type) {
    return new HealthBadgeResponse(type, "NOT_CALCULATED", false, null, null, null, null,
      null, null, null, null, null, null, null);
  }

  public static HealthBadgeResponse unavailable(String type) {
    return new HealthBadgeResponse(type, "UNAVAILABLE", false, null, null, null, null,
      null, null, null, null, null, null, null);
  }
}
