package com.penelopec.penelopemobileapi.catalog.application;

import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.SpringAdvertisementRepository;
import com.penelopec.penelopemobileapi.shared.core.exception.DomainError;
import com.penelopec.penelopemobileapi.shared.core.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstateBadgeService {
  private static final Logger LOGGER = LoggerFactory.getLogger(EstateBadgeService.class);
  static final int SCALE_MAX = 10;
  private static final Set<String> CALCULATED_STATUSES = Set.of("CALCULATED", "MEASURED");

  private record Definition(String type, String label, Map<String, String> evidenceKeys) {
  }

  private static final List<Definition> DEFINITIONS = List.of(
    new Definition("EDUCATION", "Escolas próximas", keys(
      "schools_within_radius", "schoolsWithinRadius", "nearest_meters", "nearestMeters")),
    new Definition("HEALTH_PRIMARY_CARE", "Unidades de saúde (UBS)", keys(
      "facility_count_within_radius", "facilitiesWithinRadius", "nearest_meters", "nearestMeters",
      "source_reference_period", "sourceReferencePeriod")),
    new Definition("HEALTH_GENERAL_HOSPITAL", "Hospital geral", keys(
      "facility_count_within_radius", "facilitiesWithinRadius", "nearest_meters", "nearestMeters",
      "source_reference_period", "sourceReferencePeriod")),
    new Definition("GREEN_PARK_NEARBY", "Áreas verdes", keys(
      "nearest_distance_meters", "nearestDistanceMeters", "nearest_name", "nearestName")),
    new Definition("TRANSPORT_ACCESS", "Transporte público", keys(
      "rail_stations_within_radius", "railStationsWithinRadius", "rail_nearest_meters", "railNearestMeters",
      "bus_stops_within_radius", "busStopsWithinRadius", "bus_nearest_meters", "busNearestMeters")),
    new Definition("SAFETY_OCCURRENCES", "Segurança", keys(
      "occurrences_within_radius", "occurrencesWithinRadius", "statistics_year_from", "statisticsYearFrom",
      "statistics_year_to", "statisticsYearTo", "top_natures", "topNatures"))
  );

  private final SpringAdvertisementRepository advertisements;
  private final BadgeReader reader;

  @Transactional(readOnly = true)
  public EstateBadgesResponse findByEstateId(Long estateId) {
    if (!advertisements.existsByEstateIdAndActiveTrue(estateId)) {
      throw new NotFoundException(DomainError.of("CATALOG_404", "Imóvel não encontrado."));
    }
    if (!reader.isEnabled()) {
      return unavailableAll(estateId, "DISABLED");
    }
    Map<String, BadgeRecord> records;
    try {
      records = reader.findByEstateId(estateId).stream()
        .collect(Collectors.toMap(BadgeRecord::type, Function.identity(), (first, second) -> first));
    } catch (DataAccessException error) {
      LOGGER.warn("Falha ao consultar as insígnias da unidade {}", estateId, error);
      return unavailableAll(estateId, "SOURCE_ERROR");
    }
    List<BadgeResponse> badges = DEFINITIONS.stream()
      .map(definition -> toResponse(definition, records.get(definition.type())))
      .toList();
    return new EstateBadgesResponse(estateId, SCALE_MAX, badges);
  }

  private EstateBadgesResponse unavailableAll(Long estateId, String reason) {
    return new EstateBadgesResponse(estateId, SCALE_MAX, DEFINITIONS.stream()
      .map(definition -> unavailable(definition, reason))
      .toList());
  }

  private BadgeResponse toResponse(Definition definition, BadgeRecord record) {
    if (record == null) {
      return unavailable(definition, "NOT_CALCULATED");
    }
    if (!CALCULATED_STATUSES.contains(record.calculationStatus()) || record.score10() == null) {
      return unavailable(definition, record.calculationStatus());
    }
    Map<String, Object> evidence = evidenceOf(definition, record);
    return new BadgeResponse(
      definition.type(), definition.label(), record.score10(), record.granted(),
      "CALCULATED", null, BadgeExplanation.of(definition.type(), record, evidence), evidence);
  }

  private BadgeResponse unavailable(Definition definition, String reason) {
    return new BadgeResponse(
      definition.type(), definition.label(), null, false,
      "UNAVAILABLE", reason, BadgeExplanation.unavailable(definition.label(), reason), Map.of());
  }

  private static Map<String, Object> evidenceOf(Definition definition, BadgeRecord record) {
    Map<String, Object> evidence = new LinkedHashMap<>();
    evidence.put("radiusMeters", record.radiusMeters());
    definition.evidenceKeys().forEach((source, target) -> {
      Object value = record.evidence().get(source);
      if (value != null) {
        evidence.put(target, value);
      }
    });
    evidence.put("ruleVersion", record.ruleVersion());
    evidence.put("calculatedAt", record.calculatedAt());
    return evidence;
  }

  private static Map<String, String> keys(String... pairs) {
    Map<String, String> mapping = new LinkedHashMap<>();
    for (int index = 0; index < pairs.length; index += 2) {
      mapping.put(pairs[index], pairs[index + 1]);
    }
    return mapping;
  }
}
