package com.penelopec.penelopemobileapi.shared.core.time;

import java.time.Duration;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utilitário stateless para formatação humanizada de intervalos de tempo.
 * Traduz os padrões ISO-8601 de Period e Duration para linguagem fluente (PT-BR).
 */
public final class PeriodFormatter {

  private PeriodFormatter() {
    throw new UnsupportedOperationException("Classe utilitária não deve ser instanciada.");
  }

  /**
   * Formata um Period em texto humanizado.
   * Exemplo: Period.of(1, 0, 5) -> "1 ano e 5 dias"
   */
  public static String formatHuman(Period period) {
    Objects.requireNonNull(period, "Period não pode ser nulo.");
    if (period.isZero()) {
      return "0 dias";
    }

    List<String> parts = new ArrayList<>();
    if (period.getYears() > 0) {
      parts.add(period.getYears() + (period.getYears() == 1 ? " ano" : " anos"));
    }
    if (period.getMonths() > 0) {
      parts.add(period.getMonths() + (period.getMonths() == 1 ? " mês" : " meses"));
    }
    if (period.getDays() > 0) {
      parts.add(period.getDays() + (period.getDays() == 1 ? " dia" : " dias"));
    }

    if (parts.size() == 1) {
      return parts.getFirst();
    }

    String last = parts.removeLast();
    return String.join(", ", parts) + " e " + last;
  }

  /**
   * Formata um Duration no formato de cronômetro padrão (HH:mm:ss).
   * Exemplo: Duration.ofMinutes(65) -> "01:05:00"
   */
  public static String formatStopwatch(Duration duration) {
    Objects.requireNonNull(duration, "Duration não pode ser nula.");
    long hours = duration.toHours();
    long minutes = duration.toMinutesPart();
    long seconds = duration.toSecondsPart();

    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
  }

  public static String formatDaysOnly(Duration duration) {
    long hours = duration.toHours();
    long days = hours % 24;

    return String.format("%d dia(s)", days);
  }
}
