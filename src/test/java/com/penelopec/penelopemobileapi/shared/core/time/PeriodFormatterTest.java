package com.penelopec.penelopemobileapi.shared.core.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PeriodFormatter - Formatação de Intervalos de Tempo")
class PeriodFormatterTest {

  @Nested
  @DisplayName("Testes de formatação de Period (Tempo Humano)")
  class PeriodTests {

    @Test
    @DisplayName("Deve formatar corretamente anos, meses e dias no plural e singular")
    void shouldFormatPeriodWhenMultiplePartsExist() {
      Period p1 = Period.of(1, 2, 1);
      assertThat(PeriodFormatter.formatHuman(p1)).isEqualTo("1 ano, 2 meses e 1 dia");

      Period p2 = Period.of(0, 5, 10);
      assertThat(PeriodFormatter.formatHuman(p2)).isEqualTo("5 meses e 10 dias");
    }

    @Test
    @DisplayName("Deve retornar 0 dias quando o Period for vazio")
    void shouldReturnZeroDaysWhenPeriodIsZero() {
      assertThat(PeriodFormatter.formatHuman(Period.ZERO)).isEqualTo("0 dias");
    }
  }

  @Nested
  @DisplayName("Testes de formatação de Duration (Tempo de Máquina)")
  class DurationTests {

    @Test
    @DisplayName("Deve formatar em HH:mm:ss lidando com partes de horas e minutos (Java 9+)")
    void shouldFormatStopwatchCorrectly() {
      Duration dur = Duration.ofHours(2).plusMinutes(5).plusSeconds(30);
      assertThat(PeriodFormatter.formatStopwatch(dur)).isEqualTo("02:05:30");
    }

    @Test
    @DisplayName("Deve formatar horas maiores que 24 de forma linear")
    void shouldKeepHoursAccumulated() {
      Duration dur = Duration.ofHours(26);
      assertThat(PeriodFormatter.formatStopwatch(dur)).isEqualTo("26:00:00");
    }
  }

  @Nested
  @DisplayName("Proteção Fail-Fast")
  class FailFastTests {

    @Test
    @DisplayName("Deve falhar rápido ao passar nulo")
    void shouldThrowExceptionWhenNull() {
      assertThatThrownBy(() -> PeriodFormatter.formatHuman(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Period não pode ser nulo");
    }
  }
}