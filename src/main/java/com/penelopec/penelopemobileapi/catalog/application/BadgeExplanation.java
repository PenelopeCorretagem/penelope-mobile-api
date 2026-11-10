package com.penelopec.penelopemobileapi.catalog.application;

import java.util.List;
import java.util.Map;

/** Textos em pt-BR exibidos no modal de cada insígnia. */
final class BadgeExplanation {
  private BadgeExplanation() {
  }

  static String of(String type, BadgeRecord record, Map<String, Object> evidence) {
    String radius = distance(record.radiusMeters());
    return switch (type) {
      case "EDUCATION" -> proximity(count(evidence, "schoolsWithinRadius"), "escola", "escolas",
        "Nenhuma escola", radius, number(evidence, "nearestMeters"));
      case "HEALTH_PRIMARY_CARE" -> proximity(count(evidence, "facilitiesWithinRadius"),
        "unidade básica de saúde", "unidades básicas de saúde",
        "Nenhuma unidade básica de saúde", radius, number(evidence, "nearestMeters"));
      case "HEALTH_GENERAL_HOSPITAL" -> proximity(count(evidence, "facilitiesWithinRadius"),
        "hospital geral", "hospitais gerais", "Nenhum hospital geral", radius,
        number(evidence, "nearestMeters"));
      case "GREEN_PARK_NEARBY" -> green(evidence);
      case "TRANSPORT_BUS_NEARBY", "TRANSPORT_RAIL_NEARBY" -> transport(type, evidence, radius);
      case "SAFETY_OCCURRENCES" -> safety(evidence, radius);
      default -> "Nota calculada a partir de dados públicos do entorno do imóvel.";
    };
  }

  static String unavailable(String label, String reason) {
    return switch (reason == null ? "NOT_CALCULATED" : reason) {
      case "OUT_OF_COVERAGE", "NO_MUNICIPALITY", "COORDINATES_OUTSIDE_COVERAGE" ->
        "Ainda não temos dados de " + label.toLowerCase() + " para a região deste imóvel.";
      case "NO_COORDINATES", "INVALID_COORDINATES", "LOW_PRECISION" ->
        "A localização deste imóvel não é precisa o bastante para calcular a nota de "
          + label.toLowerCase() + ".";
      case "SOURCE_ERROR" -> "Não foi possível consultar a nota de " + label.toLowerCase() + " agora.";
      case "DISABLED" -> "As notas do entorno estão desativadas neste ambiente.";
      case "INCOMPLETE_PERIOD" -> "O ano da base de registros está incompleto; nota indisponível.";
      case "LOW_SOURCE_COVERAGE", "NO_GEOLOCATED_SOURCE", "NO_ELIGIBLE_SOURCE", "NO_CATEGORY_DATA" ->
        "A fonte não contém dados geolocalizados suficientes para esta avaliação.";
      default -> "A nota de " + label.toLowerCase() + " ainda não foi calculada para este imóvel.";
    };
  }

  private static String proximity(
    int count, String singular, String plural, String none, String radius, Double nearest) {
    if (count == 0) {
      return none + " em até " + radius + ".";
    }
    String found = count == 1 ? "1 " + singular : count + " " + plural;
    String suffix = nearest == null ? "." : "; a mais próxima fica a " + distance(nearest) + ".";
    return found + " em até " + radius + suffix;
  }

  private static String green(Map<String, Object> evidence) {
    Double distance = number(evidence, "nearestDistanceMeters");
    if (distance == null) {
      return "Distância ao parque mais próximo indisponível.";
    }
    Object name = evidence.get("nearestName");
    String target = name == null ? "O parque mais próximo" : "O parque mais próximo (" + name + ")";
    return target + " fica a " + distance(distance) + " em linha reta; quanto mais perto, maior a nota.";
  }

  private static String transport(String type, Map<String, Object> evidence, String radius) {
    boolean bus = type.equals("TRANSPORT_BUS_NEARBY");
    Double nearest = number(evidence, "nearestMeters");
    String result = nearest == null
      ? "Nenhum " + (bus ? "ponto de ônibus cadastrado" : "registro de estação em operação") + " em até " + radius
      : (bus ? "Ponto de ônibus cadastrado" : "Metrô ou trem informado em operação")
        + " a " + distance(nearest) + " em linha reta";
    return result + (bus ? ". A fonte não informa a operação ou frequência do ponto."
      : ". Situação operacional informada pela fonte; distância não representa trajeto.");
  }

  private static String safety(Map<String, Object> evidence, String radius) {
    int occurrences = count(evidence, "occurrencesWithinRadius");
    Object from = evidence.get("statisticsYearFrom");
    Object to = evidence.get("statisticsYearTo");
    String period = from == null ? "" : from.equals(to) ? " em " + from : " entre " + from + " e " + to;
    String found = occurrences == 0 ? "Nenhuma ocorrência registrada"
      : occurrences == 1 ? "1 ocorrência registrada" : occurrences + " ocorrências registradas";
    return found + " com coordenadas válidas em até " + radius + period
      + ". Nota histórica baseada em registros da fonte, sem comprovar BOs distintos ou a segurança atual do imóvel.";
  }

  private static int count(Map<String, Object> evidence, String key) {
    Double value = number(evidence, key);
    return value == null ? 0 : value.intValue();
  }

  private static Double number(Map<String, Object> evidence, String key) {
    return evidence.get(key) instanceof Number value ? value.doubleValue() : null;
  }

  private static String distance(Integer meters) {
    return meters == null ? "raio da região" : distance(meters.doubleValue());
  }

  private static String distance(double meters) {
    if (meters < 1000) {
      return Math.round(meters) + " m";
    }
    double km = Math.round(meters / 100.0) / 10.0;
    return (km == Math.floor(km) ? String.valueOf((long) km) : String.valueOf(km).replace('.', ',')) + " km";
  }
}
