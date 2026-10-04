package com.penelopec.penelopemobileapi.catalog.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

public record AdvertisementResponse(
  Long id,
  BigDecimal price,
  boolean active,
  boolean featured,
  LocalDateTime createdAt,
  EstateResponse estate
) {
  public record EstateResponse(
    Long id,
    String title,
    String description,
    Double area,
    Integer numberOfRooms,
    String type,
    AddressResponse address,
    Set<MediaResponse> images,
    Set<AmenityResponse> amenities,
    @JsonInclude(JsonInclude.Include.NON_NULL) EducationBadgeResponse educationBadge
  ) {
  }

  public record AddressResponse(
    String city,
    String region,
    String uf,
    String municipalityIbgeCode,
    Double latitude,
    Double longitude,
    String coordinateSource,
    String coordinatePrecision,
    LocalDateTime coordinateUpdatedAt
  ) {
  }

  public record MediaResponse(Long id, String url, String type) {
  }

  public record AmenityResponse(Long id, String description, String icon) {
  }
}
