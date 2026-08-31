package com.penelopec.penelopemobileapi.shared.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CoreErrorCode")
class CoreErrorCodeTest {

  @Nested
  @DisplayName("Quando os métodos da constante são acessados")
  class EnumState {

    @Test
    @DisplayName("Deve retornar o código e a mensagem configurados via construtor")
    void shouldReturnConfiguredCodeAndMessage() {
      CoreErrorCode notFound = CoreErrorCode.RESOURCE_NOT_FOUND;

      assertThat(notFound.code()).isEqualTo("CORE_404");
      assertThat(notFound.defaultMessage()).isEqualTo("O recurso solicitado não foi encontrado.");
    }
  }

  @Nested
  @DisplayName("Quando métodos default da interface são invocados")
  class InterfaceDefaultMethods {

    @Test
    @DisplayName("Deve construir um DomainError simples usando toError()")
    void shouldBuildSimpleDomainError() {
      DomainError error = CoreErrorCode.VALIDATION_FAILED.toError();

      assertThat(error.code()).isEqualTo("CORE_400");
      assertThat(error.message()).isEqualTo("Os dados fornecidos não são válidos.");
      assertThat(error.metadata()).isEmpty();
    }

    @Test
    @DisplayName("Deve construir um DomainError com metadata usando toError(Map)")
    void shouldBuildDomainErrorWithMetadata() {
      Map<String, Object> meta = Map.of("field", "email");
      DomainError error = CoreErrorCode.VALIDATION_FAILED.toError(meta);

      assertThat(error.metadata())
        .isNotEmpty()
        .containsEntry("field", "email");
    }
  }
}