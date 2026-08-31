package com.penelopec.penelopemobileapi.shared.core.result;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Tipo de Dado Algébrico (ADT) para encapsular Sucesso (T) ou Falha (E).
 * Força o tratamento explícito de erros e elimina o uso de exceções para fluxo de controle.
 */
public sealed interface Result<T, E> permits Result.Success, Result.Failure {

  static <T, E> Result<T, E> success(T value) {
    return new Success<>(
      Objects.requireNonNull(value, "Result: valor de sucesso não pode ser nulo.")
    );
  }

  static <E> Result<Void, E> success() {
    return new Success<>(Void.singleton());
  }

  static <T, E> Result<T, E> failure(E error) {
    return new Failure<>(
      Objects.requireNonNull(error, "Result: erro não pode ser nulo.")
    );
  }

  default boolean isSuccess() {
    return this instanceof Success<?, ?>;
  }

  default boolean isFailure() {
    return this instanceof Failure<?, ?>;
  }

  <U> Result<U, E> map(Function<? super T, ? extends U> mapper);

  <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper);

  <F> Result<T, F> mapError(Function<? super E, ? extends F> mapper);

  <U> U fold(Function<? super T, ? extends U> onSuccess, Function<? super E, ? extends U> onFailure);

  default Result<T, E> onSuccess(Consumer<? super T> action) {
    Objects.requireNonNull(action, "Ação de sucesso não pode ser nula.");
    if (this instanceof Success<T, E>(T value)) {
      action.accept(value);
    }
    return this;
  }

  default Result<T, E> onFailure(Consumer<? super E> action) {
    Objects.requireNonNull(action, "Ação de falha não pode ser nula.");
    if (this instanceof Failure<T, E>(E error)) {
      action.accept(error);
    }
    return this;
  }

  /**
   * Válvula de escape: extrai o valor em caso de sucesso ou lança a exceção fornecida em caso de falha.
   */
  T orElseThrow(Function<? super E, ? extends RuntimeException> exceptionSupplier);

  /**
   * Resiliência: em caso de falha, tenta recuperar o fluxo fornecendo um valor alternativo de sucesso.
   */
  Result<T, E> recover(Function<? super E, ? extends T> recoveryFunction);

  /**
   * Ponte com o ecossistema nativo do Java 8+.
   */
  Optional<T> toOptional();


  // --- Record Success ---
  record Success<T, E>(T value) implements Result<T, E> {

    @Override
    public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
      Objects.requireNonNull(mapper, "Mapper não pode ser nulo");
      return Result.success(mapper.apply(value));
    }

    @Override
    public <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper) {
      Objects.requireNonNull(mapper, "Mapper não pode ser nulo");
      return Objects.requireNonNull(mapper.apply(value), "flatMap não pode retornar null");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <F> Result<T, F> mapError(Function<? super E, ? extends F> mapper) {
      return (Result<T, F>) this;
    }

    @Override
    public <U> U fold(Function<? super T, ? extends U> onSuccess, Function<? super E, ? extends U> onFailure) {
      Objects.requireNonNull(onSuccess, "Callback de sucesso não pode ser nulo");
      return onSuccess.apply(value);
    }

    @Override
    public T orElseThrow(Function<? super E, ? extends RuntimeException> exceptionSupplier) {
      return value; // Sucesso não lança exceção, apenas retorna.
    }

    @Override
    public Result<T, E> recover(Function<? super E, ? extends T> recoveryFunction) {
      return this; // Já é sucesso, ignora a recuperação.
    }

    @Override
    public Optional<T> toOptional() {
      return Optional.of(value);
    }
  }

  // --- Record Failure ---
  record Failure<T, E>(E error) implements Result<T, E> {

    @Override
    @SuppressWarnings("unchecked")
    public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
      return (Result<U, E>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper) {
      return (Result<U, E>) this;
    }

    @Override
    public <F> Result<T, F> mapError(Function<? super E, ? extends F> mapper) {
      Objects.requireNonNull(mapper, "Mapper de erro não pode ser nulo");
      return Result.failure(mapper.apply(error));
    }

    @Override
    public <U> U fold(Function<? super T, ? extends U> onSuccess, Function<? super E, ? extends U> onFailure) {
      Objects.requireNonNull(onFailure, "Callback de falha não pode ser nulo");
      return onFailure.apply(error);
    }

    @Override
    public T orElseThrow(Function<? super E, ? extends RuntimeException> exceptionSupplier) {
      Objects.requireNonNull(exceptionSupplier, "Supplier de exceção não pode ser nulo");
      throw exceptionSupplier.apply(error);
    }

    @Override
    public Result<T, E> recover(Function<? super E, ? extends T> recoveryFunction) {
      Objects.requireNonNull(recoveryFunction, "Função de recuperação não pode ser nula");
      return Result.success(recoveryFunction.apply(error));
    }

    @Override
    public Optional<T> toOptional() {
      return Optional.empty(); // O erro morre aqui, vira ausência de valor nativa do Java.
    }
  }
}