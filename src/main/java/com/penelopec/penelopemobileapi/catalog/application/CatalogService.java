package com.penelopec.penelopemobileapi.catalog.application;

import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.AdvertisementJpaEntity;
import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.AmenityJpaEntity;
import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.EstateJpaEntity;
import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.EstateMediaJpaEntity;
import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.EstateType;
import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.SpringAdvertisementRepository;
import com.penelopec.penelopemobileapi.shared.core.exception.DomainError;
import com.penelopec.penelopemobileapi.shared.core.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogService {
  private final SpringAdvertisementRepository advertisements;

  @Transactional(readOnly = true)
  public List<AdvertisementResponse> findAll(EstateType type, boolean active) {
    return advertisements.findCatalog(active, type).stream()
      .map(this::toResponse)
      .toList();
  }

  @Transactional(readOnly = true)
  public AdvertisementResponse findActiveById(Long id) {
    return advertisements.findByIdAndActiveTrue(id)
      .map(this::toResponse)
      .orElseThrow(() -> new NotFoundException(DomainError.of("CATALOG_404", "Anúncio não encontrado.")));
  }

  private AdvertisementResponse toResponse(AdvertisementJpaEntity advertisement) {
    EstateJpaEntity estate = advertisement.getEstate();
    return new AdvertisementResponse(
      advertisement.getId(),
      advertisement.getPrice(),
      advertisement.isActive(),
      advertisement.isFeatured(),
      advertisement.getCreatedAt(),
      new AdvertisementResponse.EstateResponse(
        estate.getTitle(),
        estate.getDescription(),
        estate.getArea(),
        estate.getNumberOfRooms(),
        estate.getType().name(),
        new AdvertisementResponse.AddressResponse(
          estate.getAddress().getCity(),
          estate.getAddress().getRegion(),
          estate.getAddress().getState(),
          estate.getAddress().getLatitude(),
          estate.getAddress().getLongitude()
        ),
        toMediaResponses(estate.getMedia()),
        toAmenityResponses(estate.getAmenities())
      )
    );
  }

  private Set<AdvertisementResponse.MediaResponse> toMediaResponses(Set<EstateMediaJpaEntity> media) {
    return media.stream()
      .map(item -> new AdvertisementResponse.MediaResponse(item.getId(), item.getUrl(), item.getType().getDescription()))
      .collect(Collectors.toUnmodifiableSet());
  }

  private Set<AdvertisementResponse.AmenityResponse> toAmenityResponses(Set<AmenityJpaEntity> amenities) {
    return amenities.stream()
      .map(item -> new AdvertisementResponse.AmenityResponse(item.getId(), item.getDescription(), item.getIcon()))
      .collect(Collectors.toUnmodifiableSet());
  }
}