package com.penelopec.penelopemobileapi.shared.core.object;

import com.penelopec.penelopemobileapi.shared.core.exception.DomainError;
import com.penelopec.penelopemobileapi.shared.core.result.Result;
import com.penelopec.penelopemobileapi.shared.core.validation.ValidationResult;
import com.penelopec.penelopemobileapi.shared.core.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AbstractBuilder")
class AbstractBuilderTest {

  public record User(String id, String email, boolean active) {}

  public class UserBuilder extends AbstractBuilder<User, UserBuilder> {

    private String email;
    private boolean active = false;

    public UserBuilder withEmail(String email) {
      this.email = email;
      return this;
    }

    public UserBuilder activate() {
      this.active = true;
      return this;
    }

    @Override
    protected UserBuilder self() { return this; }

    @Override
    protected User doBuild() {
      return new User(this.id, this.email, this.active);
    }

    // A MÁGICA ACONTECE AQUI: Composição de validadores
    @Override
    protected Validator<User> validator() {
      Validator<User> idNotNull = u -> u.id() != null
        ? ValidationResult.valid()
        : ValidationResult.invalid("O ID é obrigatório");

      Validator<User> emailValid = u -> u.email() != null && u.email().contains("@")
        ? ValidationResult.valid()
        : ValidationResult.invalid("E-mail inválido");

      return idNotNull.and(emailValid);
    }
  }

  @Nested
  @DisplayName("Quando utilizando o fluxo Clássico (build)")
  class ClassicBuildCases {

    @Test
    @DisplayName("Deve construir com sucesso se validações passarem")
    void shouldBuildSuccessfully() {
      User user = new UserBuilder()
        .withId("123")
        .withEmail("teste@teste.com")
        .build();

      assertThat(user.id()).isEqualTo("123");
      assertThat(user.email()).isEqualTo("teste@teste.com");
    }

    @Test
    @DisplayName("Deve aplicar Fail-Fast e lançar exceção se inválido")
    void shouldFailFastOnInvalidState() {
      assertThatThrownBy(() -> new UserBuilder().withId("123").withEmail("invalido").build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("E-mail inválido");
    }
  }

  @Nested
  @DisplayName("Quando utilizando o fluxo Funcional (safeBuild)")
  class SafeBuildCases {

    @Test
    @DisplayName("Deve retornar Result.Failure com DomainError em caso de falha")
    void shouldReturnFailureResult() {
      Result<User, DomainError> result = new UserBuilder()
        // Faltou passar o Email
        .withId("123")
        .safeBuild();

      result.onFailure(error -> {
        assertThat(error.code()).isEqualTo("VALIDATION_ERROR");
        assertThat(error.message()).isEqualTo("E-mail inválido");
      });
    }
  }

  @Nested
  @DisplayName("Quando utilizando Utilitários de Fluxo (applyIf / apply)")
  class UtilityCases {

    @Test
    @DisplayName("Deve aplicar passo condicionalmente sem quebrar a Fluent API")
    void shouldApplyConditionally() {
      boolean shouldActivate = true;
      boolean shouldAddEmail = false;

      User user = new UserBuilder()
        .withId("999")
        .applyIf(shouldActivate, UserBuilder::activate)
        .applyIf(shouldAddEmail, b -> b.withEmail("admin@site.com"))
        .withEmail("fallback@site.com") // Sobrescreve pelo fluxo principal
        .build();

      assertThat(user.active()).isTrue();
      assertThat(user.email()).isEqualTo("fallback@site.com");
    }
  }
}