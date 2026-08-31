package com.penelopec.penelopemobileapi.shared.core.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TimeUtils - Manipulação de Fusos e Formatos")
class TimeUtilsTest {

  @Nested
  @DisplayName("Conversão de Fuso Horário")
  class TimezoneTests {

    @Test
    @DisplayName("Deve converter Instant UTC para Fuso Horário de São Paulo corretamente")
    void shouldConvertInstantToBrtZonedDateTime() {
      // Instant: 15 de Junho de 2026, 12:00:00 UTC
      Instant instant = Instant.parse("2026-06-15T12:00:00Z");

      ZonedDateTime brtTime = TimeUtils.toBrt(instant);

      assertThat(brtTime.getHour()).isEqualTo(9); // UTC-3
      assertThat(brtTime.getZone()).isEqualTo(TimeUtils.BRT_ZONE);
    }
  }

  @Nested
  @DisplayName("Formatação e Parse (PT-BR)")
  class FormattingTests {

    @Test
    @DisplayName("Deve formatar LocalDate para String dd/MM/yyyy")
    void shouldFormatLocalDateToBrString() {
      LocalDate date = LocalDate.of(2026, 6, 15);
      assertThat(TimeUtils.formatBr(date)).isEqualTo("15/06/2026");
    }

    @Test
    @DisplayName("Deve realizar parse de String dd/MM/yyyy para LocalDate")
    void shouldParseBrStringToLocalDate() {
      String dateStr = "15/06/2026";
      LocalDate date = TimeUtils.parseBr(dateStr);

      assertThat(date).isEqualTo(LocalDate.of(2026, 6, 15));
    }

    @Test
    @DisplayName("Deve lançar exceção ao fazer parse de data inválida")
    void shouldThrowExceptionWhenParsingInvalidDate() {
      assertThatThrownBy(() -> TimeUtils.parseBr("2026-06-15")) // Formato ISO, esperando BR
        .isInstanceOf(DateTimeParseException.class);
    }
  }

  @Test
  @DisplayName("Deve formatar Instant para ISO com offset no fuso informado")
  void shouldFormatIsoWithOffsetForGivenZone() {
    Instant instant = Instant.parse("2026-06-15T12:00:00Z");

    String formatted = TimeUtils.formatIsoWithOffset(instant, TimeUtils.BRT_ZONE);

    assertThat(formatted).isEqualTo("2026-06-15T09:00:00-03:00");
  }

  @Test
  @DisplayName("Deve falhar rápido se instant for nulo")
  void shouldThrowWhenInstantIsNullInFormatIsoWithOffset() {
    assertThatThrownBy(() -> TimeUtils.formatIsoWithOffset(null, TimeUtils.BRT_ZONE))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("Instant não pode ser nulo.");
  }

  @Test
  @DisplayName("Deve falhar rápido se zone for nulo")
  void shouldThrowWhenZoneIsNullInFormatIsoWithOffset() {
    Instant instant = Instant.parse("2026-06-15T12:00:00Z");

    assertThatThrownBy(() -> TimeUtils.formatIsoWithOffset(instant, null))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("ZoneId não pode ser nulo.");
  }
}