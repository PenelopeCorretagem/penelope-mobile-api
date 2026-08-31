package com.penelopec.penelopemobileapi.shared.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DomainError")
class DomainErrorTest {

  @Nested
  @DisplayName("Quando instanciado com sucesso")
  class SuccessfulInstantiation {

    @Test
    @DisplayName("Deve criar o erro corretamente usando o factory method")
    void shouldCreateErrorWhenUsingFactoryMethod() {
      DomainError error =
        DomainError.of("ERR_01", "Falha de validação");

      assertThat(error.code()).isEqualTo("ERR_01");
      assertThat(error.message()).isEqualTo("Falha de validação");
      assertThat(error.metadata()).isEmpty();
    }
  }

  @Nested
  @DisplayName("Quando instanciado com parâmetros inválidos (Fail-Fast)")
  class InvalidInstantiation {

    @Test
    @DisplayName("Deve falhar se o code for nulo")
    void shouldThrowExceptionWhenCodeIsNull() {
      assertThatThrownBy(() ->
        DomainError.of(null, "Mensagem"))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("code não pode ser nulo");
    }
  }

  @Nested
  @DisplayName("Garantias de Imutabilidade")
  class ImmutabilityGuarantees {

    @Test
    @DisplayName("Deve impedir a modificação do metadata original após a criação")
    void shouldPreventMetadataModificationWhenOriginalMapChanges() {
      Map<String, Object> modifiableMap = new HashMap<>();
      modifiableMap.put("userId", 123);

      DomainError error =
        new DomainError(
          "ERR_02",
          "Erro interno",
          modifiableMap
        );

      modifiableMap.put("hacked", true);

      assertThat(error.metadata())
        .containsEntry("userId", 123)
        .doesNotContainKey("hacked");
    }

    @Test
    @DisplayName("Deve retornar metadata imutável")
    void shouldReturnImmutableMetadata() {
      DomainError error =
        DomainError.of("ERR_03", "Erro")
          .withMetadata("userId", 123);

      assertThatThrownBy(() ->
        error.metadata().put("hack", true))
        .isInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Nested
  @DisplayName("Quando adicionar metadata")
  class MetadataEnrichment {

    @Test
    @DisplayName("Deve retornar uma nova instância preservando o objeto original")
    void shouldReturnNewInstancePreservingOriginalObject() {
      DomainError original =
        DomainError.of("ERR_04", "Erro");

      DomainError updated =
        original.withMetadata("userId", 123);

      assertThat(updated)
        .isNotSameAs(original);

      assertThat(original.metadata())
        .isEmpty();

      assertThat(updated.metadata())
        .containsEntry("userId", 123);
    }

    @Test
    @DisplayName("Deve preservar os metadados existentes ao adicionar um novo")
    void shouldPreserveExistingMetadataWhenAddingNewOne() {
      DomainError original =
        DomainError.of("ERR_05", "Erro")
          .withMetadata("traceId", "abc");

      DomainError updated =
        original.withMetadata("userId", 123);

      assertThat(updated.metadata())
        .containsEntry("traceId", "abc")
        .containsEntry("userId", 123);
    }

    @Test
    @DisplayName("Deve substituir o valor quando a chave já existir")
    void shouldReplaceMetadataValueWhenKeyAlreadyExists() {
      DomainError original =
        DomainError.of("ERR_06", "Erro")
          .withMetadata("attempt", 1);

      DomainError updated =
        original.withMetadata("attempt", 2);

      assertThat(updated.metadata())
        .containsEntry("attempt", 2);

      assertThat(original.metadata())
        .containsEntry("attempt", 1);
    }
  }

  @Nested
  @DisplayName("Quando adicionar metadata inválido (Fail-Fast)")
  class InvalidMetadataEnrichment {

    @Test
    @DisplayName("Deve falhar se a chave for nula")
    void shouldThrowExceptionWhenMetadataKeyIsNull() {
      DomainError error =
        DomainError.of("ERR_07", "Erro");

      assertThatThrownBy(() ->
        error.withMetadata(null, "value"))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("key não pode ser nulo");
    }
  }
}