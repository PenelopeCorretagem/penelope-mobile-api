package com.penelopec.penelopemobileapi.shared.core.validation;

import java.util.Objects;

/**
 * Contrato funcional para validação de objetos de domínio.
 *
 * @param <T> O tipo do objeto a ser validado.
 */
@FunctionalInterface
public interface Validator<T> {

  ValidationResult validate(T target);

  /**
   * Compõe este validador com outro, aplicando o padrão Fail-Fast (Curto-circuito de falha).
   * * <p>Utiliza contravariância ({@code ? super T}) para permitir que um Validator de um
   * supertipo (ex: Object) seja usado para validar um subtipo (ex: User).</p>
   *
   * @param other O próximo validador a ser executado caso este seja válido.
   * @return Um novo validador composto.
   */
  default Validator<T> and(Validator<? super T> other) {
    Objects.requireNonNull(other, "O validador secundário não pode ser nulo.");

    return target -> {
      ValidationResult currentResult = this.validate(target);
      if (!currentResult.isValid()) {
        return currentResult;
      }
      return other.validate(target);
    };
  }

  /**
   * Compõe este validador com outro, aplicando um curto-circuito de sucesso (Ou Lógico).
   *
   * <p>Se o validador atual considerar o objeto válido, a validação é encerrada
   * com sucesso imediatamente. Caso contrário, o validador secundário
   * será acionado como plano de contingência (fallback).</p>
   *
   * @param other O próximo validador a ser executado caso este falhe.
   * @return Um novo validador composto.
   */
  default Validator<T> or(Validator<? super T> other) {
    Objects.requireNonNull(other, "O validador secundário não pode ser nulo.");

    return target -> {
      ValidationResult currentResult = this.validate(target);
      if (currentResult.isValid()) {
        return currentResult;
      }
      return other.validate(target);
    };
  }
}