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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CatalogService.class);

  private final SpringAdvertisementRepository advertisements;
  private final EducationBadgeReader educationBadges;

  @Transactional(readOnly = true)
  public List<AdvertisementResponse> findAll(EstateType type, boolean active) {
    return advertisements.findCatalog(active, type).stream()
      .map(advertisement -> toResponse(advertisement, null))
      .toList();
  }

  @Transactional(readOnly = true)
  public AdvertisementResponse findActiveById(Long id) {
    AdvertisementJpaEntity advertisement = advertisements.findByIdAndActiveTrue(id)
      .orElseThrow(() -> new NotFoundException(DomainError.of("CATALOG_404", "Anúncio não encontrado.")));
    EducationBadgeResponse educationBadge;
    try {
      educationBadge = educationBadges.findByEstateId(advertisement.getEstate().getId()).orElse(null);
    } catch (DataAccessException error) {
      LOGGER.warn("Falha ao consultar a insígnia de educação da unidade {}", advertisement.getEstate().getId(), error);
      educationBadge = EducationBadgeResponse.unavailable();
    }
    return toResponse(advertisement, educationBadge);
  }

  private AdvertisementResponse toResponse(
    AdvertisementJpaEntity advertisement, EducationBadgeResponse educationBadge) {
    EstateJpaEntity estate = advertisement.getEstate();
    return new AdvertisementResponse(
      advertisement.getId(),
      advertisement.getPrice(),
      advertisement.isActive(),
      advertisement.isFeatured(),
      advertisement.getCreatedAt(),
      new AdvertisementResponse.EstateResponse(
        estate.getId(),
        estate.getTitle(),
        estate.getDescription(),
        estate.getArea(),
        estate.getNumberOfRooms(),
        estate.getType().name(),
        new AdvertisementResponse.AddressResponse(
          estate.getAddress().getCity(),
          estate.getAddress().getRegion(),
          estate.getAddress().getState(),
          estate.getAddress().getMunicipalityIbgeCode(),
          estate.getAddress().getLatitude(),
          estate.getAddress().getLongitude(),
          estate.getAddress().getCoordinateSource(),
          estate.getAddress().getCoordinatePrecision(),
          estate.getAddress().getCoordinateUpdatedAt()
        ),
        toMediaResponses(estate.getMedia()),
        toAmenityResponses(estate.getAmenities()),
        educationBadge
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
