package com.penelopec.penelopemobileapi.shared.core.time;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Representa um intervalo de tempo inclusivo e imutável entre duas datas (Tempo Humano).
 * Fornece métodos funcionais para verificação de sobreposição e geração de dias.
 */
public record DateRange(LocalDate startDate, LocalDate endDate) {

  public DateRange {
    Objects.requireNonNull(startDate, "Data de início não pode ser nula.");
    Objects.requireNonNull(endDate, "Data de fim não pode ser nula.");

    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("Data de fim não pode ser anterior à data de início.");
    }
  }

  /**
   * Factory method fluente para inicialização sem uso de construtor direto.
   */
  public static DateRange of(LocalDate startDate, LocalDate endDate) {
    return new DateRange(startDate, endDate);
  }

  /**
   * Verifica se uma data específica está dentro do intervalo (inclusivo).
   */
  public boolean contains(LocalDate date) {
    Objects.requireNonNull(date, "A data a ser verificada não pode ser nula.");
    return !date.isBefore(startDate) && !date.isAfter(endDate);
  }

  /**
   * Verifica se este intervalo se sobrepõe a outro intervalo.
   */
  public boolean overlaps(DateRange other) {
    Objects.requireNonNull(other, "O outro intervalo não pode ser nulo.");
    return !this.startDate.isAfter(other.endDate) && !this.endDate.isBefore(other.startDate);
  }

  /**
   * Retorna um Stream funcional com todos os dias contidos no intervalo.
   */
  public Stream<LocalDate> streamDays() {
    return startDate.datesUntil(endDate.plusDays(1));
  }

  /**
   * Calcula a interseção entre este intervalo e outro.
   * Retorna um Optional contendo um novo DateRange que representa exatamente
   * o período que os dois intervalos têm em comum, ou empty se não houver sobreposição.
   */
  public Optional<DateRange> intersection(DateRange other) {
    Objects.requireNonNull(other, "O outro intervalo não pode ser nulo.");

    if (!this.overlaps(other)) {
      return Optional.empty();
    }

    // A data de início da interseção é a maior entre as duas datas de início
    LocalDate intersectionStart = this.startDate.isAfter(other.startDate)
      ? this.startDate
      : other.startDate;

    // A data de fim da interseção é a menor entre as duas datas de fim
    LocalDate intersectionEnd = this.endDate.isBefore(other.endDate)
      ? this.endDate
      : other.endDate;

    return Optional.of(new DateRange(intersectionStart, intersectionEnd));
  }
}