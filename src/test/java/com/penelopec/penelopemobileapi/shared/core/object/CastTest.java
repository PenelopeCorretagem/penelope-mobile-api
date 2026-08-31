package com.penelopec.penelopemobileapi.shared.core.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cast")
class CastTest {

  @Nested
  @DisplayName("Método as")
  class As {

    @Test
    @DisplayName("Deve retornar Optional com valor quando a instância corresponder ao tipo")
    void shouldReturnOptionalWithValueWhenInstanceMatchesType() {
      Object text = "Domain Driven Design";
      Optional<String> result = Cast.as(text, String.class);

      assertThat(result).isPresent().contains("Domain Driven Design");
    }

    @Test
    @DisplayName("Deve retornar vazio quando a instância não corresponder ao tipo polimórfico")
    void shouldReturnEmptyWhenInstanceDoesNotMatchPolymorphicType() {
      Object number = 42;
      Optional<String> result = Cast.as(number, String.class);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar vazio com segurança ao lidar com valores nulos")
    void shouldReturnEmptyWhenHandlingNulls() {
      assertThat(Cast.as(null, String.class)).isEmpty();
      assertThat(Cast.as("Test", null)).isEmpty();
      assertThat(Cast.as(null, null)).isEmpty();
    }

    @Test
    @DisplayName("Deve respeitar o polimorfismo ao passar uma subclasse (Dynamic Dispatch)")
    void shouldRespectPolymorphismWhenPassingSubclass() {
      // Integer "É-Um" Number
      Object value = Integer.valueOf(100);
      Optional<Number> result = Cast.as(value, Number.class);

      assertThat(result).isPresent().contains(100);
    }
  }

  @Nested
  @DisplayName("Método filter")
  class Filter {

    @Test
    @DisplayName("Deve filtrar a coleção e retornar o Stream com os elementos do tipo correto convertidos")
    void shouldFilterAndCastMatchingElements() {
      // Arrange com coleção mista (String, Integer, Double)
      List<Object> mixedCollection = List.of("Texto 1", 42, "Texto 2", 99.9, 10);

      // Act
      Stream<String> stringStream = Cast.filter(mixedCollection, String.class);
      Stream<Integer> integerStream = Cast.filter(mixedCollection, Integer.class);

      // Assert
      assertThat(stringStream).containsExactly("Texto 1", "Texto 2");
      assertThat(integerStream).containsExactly(42, 10);
    }

    @Test
    @DisplayName("Deve retornar Stream vazio se a coleção original não possuir o tipo solicitado")
    void shouldReturnEmptyStreamWhenNoElementsMatch() {
      List<Object> onlyNumbers = List.of(1, 2, 3);

      Stream<String> result = Cast.filter(onlyNumbers, String.class);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar Stream vazio com segurança se a coleção ou o tipo alvo forem nulos")
    void shouldReturnEmptyStreamWhenCollectionOrTypeIsNull() {
      assertThat(Cast.filter(null, String.class)).isEmpty();
      assertThat(Cast.filter(List.of("A", "B"), null)).isEmpty();
    }
  }
}