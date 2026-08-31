package com.penelopec.penelopemobileapi.shared.core.time;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Utilitário stateless para manipulação segura de Timezones e Formatações.
 * Garante operações imutáveis e Thread-Safe protegidas contra anomalias do SO.
 */
public final class TimeUtils {

  // Instâncias estáticas, imutáveis e reaproveitáveis.
  public static final ZoneId UTC_ZONE = ZoneOffset.UTC;
  public static final ZoneId BRT_ZONE = ZoneId.of("America/Sao_Paulo");

  public static final DateTimeFormatter BR_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private TimeUtils() {
    throw new UnsupportedOperationException("Classe utilitária não deve ser instanciada.");
  }

  /**
   * Converte um tempo absoluto de máquina para um ZonedDateTime focado no Brasil.
   */
  public static ZonedDateTime toBrt(Instant instant) {
    Objects.requireNonNull(instant, "Instant não pode ser nulo.");
    return instant.atZone(BRT_ZONE);
  }

  /**
   * Formata um LocalDate para o padrão brasileiro (dd/MM/yyyy).
   */
  public static String formatBr(LocalDate date) {
    Objects.requireNonNull(date, "LocalDate não pode ser nulo.");
    return BR_DATE_FORMATTER.format(date);
  }

  /**
   * Realiza o parse seguro de uma String padrão BR para LocalDate.
   */
  public static LocalDate parseBr(String dateStr) {
    Objects.requireNonNull(dateStr, "String de data não pode ser nula.");
    return LocalDate.parse(dateStr, BR_DATE_FORMATTER);
  }

  public static String formatIsoWithOffset(Instant instant, ZoneId zone) {
    Objects.requireNonNull(instant, "Instant não pode ser nulo.");
    Objects.requireNonNull(zone, "ZoneId não pode ser nulo.");

    return instant
      .atZone(zone)
      .toOffsetDateTime()
      .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }
}
