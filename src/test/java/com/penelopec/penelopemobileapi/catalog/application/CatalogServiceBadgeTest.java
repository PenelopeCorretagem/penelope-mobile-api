package com.penelopec.penelopemobileapi.catalog.application;

import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.AddressJpaEntity;
import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.AdvertisementJpaEntity;
import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.EstateJpaEntity;
import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.EstateType;
import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.SpringAdvertisementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogServiceBadgeTest {
  private final SpringAdvertisementRepository advertisements = mock(SpringAdvertisementRepository.class);
  private final EducationBadgeReader badges = mock(EducationBadgeReader.class);
  private final CatalogService service = new CatalogService(advertisements, badges);

  @Test
  void readsBadgeUsingEstateIdOnlyForDetail() {
    AdvertisementJpaEntity advertisement = advertisement();
    when(advertisements.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(advertisement));
    when(badges.findByEstateId(42L)).thenReturn(Optional.of(EducationBadgeResponse.notCalculated()));

    AdvertisementResponse detail = service.findActiveById(1L);

    assertThat(detail.estate().educationBadge().status()).isEqualTo("NOT_CALCULATED");
    verify(badges).findByEstateId(42L);

    when(advertisements.findCatalog(true, null)).thenReturn(List.of(advertisement));
    assertThat(service.findAll(null, true).getFirst().estate().educationBadge()).isNull();
    verify(badges, never()).findByEstateId(1L);
  }

  @Test
  void keepsAdvertisementAvailableWhenBadgeDatabaseFails() {
    AdvertisementJpaEntity advertisement = advertisement();
    when(advertisements.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(advertisement));
    when(badges.findByEstateId(42L)).thenThrow(new DataAccessResourceFailureException("offline"));

    assertThat(service.findActiveById(1L).estate().educationBadge().status())
      .isEqualTo("UNAVAILABLE");
  }

  private AdvertisementJpaEntity advertisement() {
    AdvertisementJpaEntity advertisement = mock(AdvertisementJpaEntity.class);
    EstateJpaEntity estate = mock(EstateJpaEntity.class);
    AddressJpaEntity address = mock(AddressJpaEntity.class);
    when(advertisement.getId()).thenReturn(1L);
    when(advertisement.getEstate()).thenReturn(estate);
    when(estate.getId()).thenReturn(42L);
    when(estate.getType()).thenReturn(EstateType.LANCAMENTO);
    when(estate.getAddress()).thenReturn(address);
    when(estate.getMedia()).thenReturn(Set.of());
    when(estate.getAmenities()).thenReturn(Set.of());
    return advertisement;
  }
}
