package com.penelopec.penelopemobileapi.shared.core.object;

import com.penelopec.penelopemobileapi.shared.core.exception.DomainError;
import com.penelopec.penelopemobileapi.shared.core.result.Result;
import com.penelopec.penelopemobileapi.shared.core.validation.ValidationResult;
import com.penelopec.penelopemobileapi.shared.core.validation.Validator;

import java.util.function.Consumer;

/**
 * Construtor base que aplica CRTP para Fluent API, Template Method para
 * ciclo de vida padronizado e injeta validação funcional (Fail-Fast e Safe).
 *
 * @param <T> O tipo do objeto a ser construído.
 * @param <B> O tipo do Builder concreto (recursivo).
 */
public abstract class AbstractBuilder<T, B extends AbstractBuilder<T, B>> implements Builder<T> {

  protected String id;

  public B withId(String id) {
    this.id = id;
    return self();
  }

  protected abstract B self();

  /**
   * Gancho (Hook) obrigatório para a construção física do objeto.
   */
  protected abstract T doBuild();

  /**
   * Define o contrato de validação do domínio para este objeto.
   * <p>Sobrescreva este método compondo validadores via .and() ou .or().
   * O padrão é retornar um validador que aprova qualquer estado.</p>
   */
  protected Validator<T> validator() {
    return target -> ValidationResult.valid();
  }

  /**
   * Construção Clássica (Fail-Fast): Lança exceção se o objeto for inválido.
   */
  @Override
  public final T build() {
    T instance = doBuild();
    ValidationResult validation = validator().validate(instance);

    if (!validation.isValid()) {
      // Em produção, você pode lançar uma DomainValidationException customizada
      String firstError = validation.violations().messages().getFirst();
      throw new IllegalStateException("Falha de validação na construção: " + firstError);
    }

    return instance;
  }

  /**
   * Construção Monádica: Tenta construir o objeto e empacota o resultado
   * em Sucesso ou Falha, sem lançar exceções.
   */
  public final Result<T, DomainError> safeBuild() {
    try {
      T instance = doBuild();
      ValidationResult validation = validator().validate(instance);

      if (!validation.isValid()) {
        String firstError = validation.violations().messages().getFirst();
        return Result.failure(DomainError.of("VALIDATION_ERROR", firstError));
      }

      return Result.success(instance);

    } catch (Exception e) {
      return Result.failure(DomainError.of("BUILD_ERROR", "Erro fatal ao instanciar: " + e.getMessage()));
    }
  }

  // ==========================================
  // Utilitários de Fluxo (Fluent API Helpers)
  // ==========================================

  /**
   * Utilitário: Aplica um passo de construção condicionalmente.
   * Elimina a necessidade de quebrar a cadeia para fazer um "if" tradicional.
   */
  public final B applyIf(boolean condition, Consumer<? super B> action) {
    if (condition) {
      action.accept(self());
    }
    return self();
  }

  /**
   * Utilitário: Permite injetar blocos de configuração complexos na mesma cadeia.
   */
  public final B apply(Consumer<? super B> action) {
    action.accept(self());
    return self();
  }
}