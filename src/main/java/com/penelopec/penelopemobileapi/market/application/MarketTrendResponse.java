package com.penelopec.penelopemobileapi.market.application;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

public record MarketTrendResponse(Snapshot snapshot, Neighborhood neighborhood, List<Year> years) {
  public record Snapshot(long id, String ruleVersion, int startYear, int throughYear, String paymentCutoff) {}

  public record Neighborhood(String key, String name) {}

  public record Neighborhoods(Snapshot snapshot, List<Neighborhood> neighborhoods) {}

  @JsonInclude(JsonInclude.Include.ALWAYS)
  public record Year(
    int year,
    int sampleCount,
    BigDecimal medianPriceSqm,
    Integer comparisonYear,
    BigDecimal nominalChangePct,
    String status,
    String comparisonStatus
  ) {}
}
