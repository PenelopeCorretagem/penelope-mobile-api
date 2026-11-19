package com.penelopec.penelopemobileapi.catalog.application;

import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.SpringAdvertisementRepository;
import com.penelopec.penelopemobileapi.shared.core.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EstateBadgeServiceTest {
  private static final List<String> ORDER = List.of(
    "EDUCATION", "HEALTH_PRIMARY_CARE", "HEALTH_GENERAL_HOSPITAL",
    "GREEN_PARK_NEARBY", "TRANSPORT_BUS_NEARBY", "TRANSPORT_RAIL_NEARBY", "SAFETY_OCCURRENCES");

  private final SpringAdvertisementRepository advertisements = mock(SpringAdvertisementRepository.class);
  private final BadgeReader reader = mock(BadgeReader.class);
  private final EstateBadgeService service = new EstateBadgeService(advertisements, reader);

  private void estateExists() {
    when(advertisements.existsByEstateIdAndActiveTrue(42L)).thenReturn(true);
    when(reader.isEnabled()).thenReturn(true);
  }

  private static BadgeRecord record(String type, String score, String status, Map<String, Object> evidence) {
    return new BadgeRecord(type, score == null ? null : new BigDecimal(score), true, status, 1000,
      type.toLowerCase() + "-v1", LocalDateTime.of(2026, 10, 2, 10, 0), evidence);
  }

  @Test
  void alwaysReturnsSevenBadgesInFixedOrderAndMarksMissingOnesUnavailable() {
    estateExists();
    when(reader.findByEstateId(42L)).thenReturn(List.of(
      record("SAFETY_OCCURRENCES", "7.5", "CALCULATED", Map.of(
        "occurrences_within_radius", 150, "statistics_year_from", 2023, "statistics_year_to", 2023)),
      record("EDUCATION", "8.3", "CALCULATED", Map.of("schools_within_radius", 5, "nearest_meters", 450))));

    EstateBadgesResponse response = service.findByEstateId(42L);

    assertThat(response.scaleMax()).isEqualTo(10);
    assertThat(response.badges()).extracting(BadgeResponse::type).containsExactlyElementsOf(ORDER);
    BadgeResponse education = response.badges().getFirst();
    assertThat(education.status()).isEqualTo("CALCULATED");
    assertThat(education.score()).isEqualByComparingTo("8.3");
    assertThat(education.explanation()).isEqualTo("5 escolas em até 1 km; a mais próxima fica a 450 m.");
    assertThat(education.evidence()).containsEntry("schoolsWithinRadius", 5);
    BadgeResponse safety = response.badges().getLast();
    assertThat(safety.explanation()).contains("150 ocorrências registradas").contains("em 2023");
    BadgeResponse missing = response.badges().get(1);
    assertThat(missing.status()).isEqualTo("UNAVAILABLE");
    assertThat(missing.reason()).isEqualTo("NOT_CALCULATED");
    assertThat(missing.score()).isNull();
    assertThat(missing.explanation()).isNotBlank();
  }

  @Test
  void treatsEtlStatusWithoutScoreAsUnavailableWithItsReason() {
    estateExists();
    when(reader.findByEstateId(42L)).thenReturn(List.of(
      record("GREEN_PARK_NEARBY", null, "NO_COORDINATES", Map.of()),
      record("TRANSPORT_BUS_NEARBY", "0.0", "CALCULATED", Map.of())));

    List<BadgeResponse> badges = service.findByEstateId(42L).badges();

    assertThat(badges.get(3).status()).isEqualTo("UNAVAILABLE");
    assertThat(badges.get(3).reason()).isEqualTo("NO_COORDINATES");
    assertThat(badges.get(4).status()).isEqualTo("CALCULATED");
    assertThat(badges.get(4).score()).isEqualByComparingTo("0");
  }

  @Test
  void keepsRespondingWhenBadgeDatabaseFails() {
    estateExists();
    when(reader.findByEstateId(42L)).thenThrow(new DataAccessResourceFailureException("offline"));

    EstateBadgesResponse response = service.findByEstateId(42L);

    assertThat(response.badges()).hasSize(7)
      .allSatisfy(badge -> {
        assertThat(badge.status()).isEqualTo("UNAVAILABLE");
        assertThat(badge.reason()).isEqualTo("SOURCE_ERROR");
      });
  }

  @Test
  void reportsDisabledWhenFeatureIsOff() {
    when(advertisements.existsByEstateIdAndActiveTrue(42L)).thenReturn(true);
    when(reader.isEnabled()).thenReturn(false);

    assertThat(service.findByEstateId(42L).badges())
      .allSatisfy(badge -> assertThat(badge.reason()).isEqualTo("DISABLED"));
  }

  @Test
  void failsWith404WhenEstateDoesNotExist() {
    assertThatThrownBy(() -> service.findByEstateId(7L)).isInstanceOf(NotFoundException.class);
  }

  @Test
  void preservesIndependentTransportAndHealthGrantRegardlessOfGrade() {
    estateExists();
    when(reader.findByEstateId(42L)).thenReturn(List.of(
      record("TRANSPORT_BUS_NEARBY", "3.5", "CALCULATED", Map.of("nearest_meters", 250)),
      record("TRANSPORT_RAIL_NEARBY", "0.0", "CALCULATED", Map.of()),
      record("HEALTH_PRIMARY_CARE", "2.0", "CALCULATED", Map.of("facility_count_within_radius", 1))));
    List<BadgeResponse> badges = service.findByEstateId(42L).badges();
    assertThat(badges.get(1).granted()).isTrue();
    assertThat(badges.get(4).granted()).isTrue();
    assertThat(badges.get(4).explanation()).contains("250 m").contains("não informa a operação");
    assertThat(badges.get(5).type()).isEqualTo("TRANSPORT_RAIL_NEARBY");
  }

  @Test
  void legacyCalculatedBinaryBadgeKeepsGrantAndEvidenceWithoutGrade() {
    estateExists();
    when(reader.findByEstateId(42L)).thenReturn(List.of(
      record("HEALTH_PRIMARY_CARE", null, "CALCULATED", Map.of("facility_count_within_radius", 1))));
    BadgeResponse badge = service.findByEstateId(42L).badges().get(1);
    assertThat(badge.status()).isEqualTo("CALCULATED");
    assertThat(badge.reason()).isEqualTo("SCORE_PENDING");
    assertThat(badge.granted()).isTrue();
    assertThat(badge.evidence()).containsEntry("facilitiesWithinRadius", 1);
  }

  @Test
  void keepsHistoricalSourceQualityWhenScoreIsUnavailable() {
    estateExists();
    when(reader.findByEstateId(42L)).thenReturn(List.of(new BadgeRecord(
      "SAFETY_OCCURRENCES", null, false, "LOW_SOURCE_COVERAGE", 500, "safety-occurrences-v2",
      LocalDateTime.of(2026, 11, 10, 12, 0), Map.of("source", "SP_CRIME_OCCURRENCES",
        "historical_only", true, "source_quality", Map.of("geolocation_coverage_pct", 20),
        "statistics_year_from", 2023, "statistics_year_to", 2023), null, 5L)));
    BadgeResponse badge = service.findByEstateId(42L).badges().getLast();
    assertThat(badge.score()).isNull();
    assertThat(badge.status()).isEqualTo("UNAVAILABLE");
    assertThat(badge.granted()).isFalse();
    assertThat(badge.evidence()).containsEntry("sourceRunId", 5L).containsEntry("historicalOnly", true);
    assertThat(badge.evidence()).containsKey("sourceQuality");
  }
}
