package com.penelopec.penelopemobileapi.catalog.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EducationBadgeResponse(
  String status,
  BigDecimal score,
  boolean granted,
  Integer radiusMeters,
  Integer schoolsWithinRadius,
  BigDecimal nearestMeters,
  String distanceMethod,
  String ruleVersion,
  String source,
  Long sourceRunId,
  LocalDateTime sourceLoadedAt,
  LocalDateTime calculatedAt
) {
  public static EducationBadgeResponse notCalculated() {
    return new EducationBadgeResponse("NOT_CALCULATED", null, false, null, null, null, null, null, null, null, null, null);
  }

  public static EducationBadgeResponse unavailable() {
    return new EducationBadgeResponse("UNAVAILABLE", null, false, null, null, null, null, null, null, null, null, null);
  }
}
