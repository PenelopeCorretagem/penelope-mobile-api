package com.penelopec.penelopemobileapi.catalog.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record AdvertisementResponse(
  Long id,
  BigDecimal price,
  boolean active,
  boolean featured,
  LocalDateTime createdAt,
  EstateResponse estate
) {
  public record EstateResponse(
    String title,
    String description,
    Double area,
    Integer numberOfRooms,
    String type,
    AddressResponse address,
    Set<MediaResponse> images,
    Set<AmenityResponse> amenities
  ) {
  }

  public record AddressResponse(String city, String region, String uf, Double latitude, Double longitude) {
  }

  public record MediaResponse(Long id, String url, String type) {
  }

  public record AmenityResponse(Long id, String description, String icon) {
  }
}