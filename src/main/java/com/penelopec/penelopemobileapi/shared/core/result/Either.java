package com.penelopec.penelopemobileapi.shared.core.result;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Padrão de Projeto Funcional Monadic (Either).
 */
public sealed interface Either<L, R> permits Either.Left, Either.Right {

  static <L, R> Either<L, R> left(L value) {
    return new Left<>(value);
  }

  static <L, R> Either<L, R> right(R value) {
    return new Right<>(value);
  }

  boolean isLeft();
  boolean isRight();

  <T> T fold(Function<? super L, ? extends T> leftMapper, Function<? super R, ? extends T> rightMapper);

  <U> Either<L, U> mapRight(Function<? super R, ? extends U> mapper);

  <V> Either<V, R> mapLeft(Function<? super L, ? extends V> mapper);

  /**
   * Encadeia transformações que também retornam um Either,
   * achatando a estrutura (evitando Either<L, Either<L, U>>).
   */
  <U> Either<L, U> flatMapRight(Function<? super R, Either<L, U>> mapper);

  default Either<L, R> onRight(Consumer<? super R> action) {
    Objects.requireNonNull(action, "Ação não pode ser nula.");
    if (this instanceof Right<L, R>(R value)) {
      action.accept(value);
    }
    return this;
  }

  default Either<L, R> onLeft(Consumer<? super L> action) {
    Objects.requireNonNull(action, "Ação não pode ser nula.");
    if (this instanceof Left<L, R>(L value)) {
      action.accept(value);
    }
    return this;
  }

  // --- Implementações Seladas ---

  record Left<L, R>(L value) implements Either<L, R> {
    public Left {
      Objects.requireNonNull(value, "O valor Left não pode ser nulo.");
    }

    @Override public boolean isLeft() { return true; }
    @Override public boolean isRight() { return false; }

    @Override
    public <T> T fold(Function<? super L, ? extends T> leftMapper, Function<? super R, ? extends T> rightMapper) {
      Objects.requireNonNull(leftMapper, "leftMapper não pode ser nulo.");
      return leftMapper.apply(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> Either<L, U> mapRight(Function<? super R, ? extends U> mapper) {
      return (Either<L, U>) this;
    }

    @Override
    public <V> Either<V, R> mapLeft(Function<? super L, ? extends V> mapper) {
      Objects.requireNonNull(mapper, "O mapper não pode ser nulo.");
      return Either.left(mapper.apply(value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> Either<L, U> flatMapRight(Function<? super R, Either<L, U>> mapper) {
      return (Either<L, U>) this;
    }
  }

  record Right<L, R>(R value) implements Either<L, R> {
    public Right {
      Objects.requireNonNull(value, "O valor Right não pode ser nulo.");
    }

    @Override public boolean isLeft() { return false; }
    @Override public boolean isRight() { return true; }

    @Override
    public <T> T fold(Function<? super L, ? extends T> leftMapper, Function<? super R, ? extends T> rightMapper) {
      Objects.requireNonNull(rightMapper, "rightMapper não pode ser nulo.");
      return rightMapper.apply(value);
    }

    @Override
    public <U> Either<L, U> mapRight(Function<? super R, ? extends U> mapper) {
      Objects.requireNonNull(mapper, "O mapper não pode ser nulo.");
      return Either.right(mapper.apply(value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> Either<V, R> mapLeft(Function<? super L, ? extends V> mapper) {
      return (Either<V, R>) this;
    }

    @Override
    public <U> Either<L, U> flatMapRight(Function<? super R, Either<L, U>> mapper) {
      Objects.requireNonNull(mapper, "O mapper não pode ser nulo.");
      return Objects.requireNonNull(mapper.apply(value), "flatMapRight não pode retornar nulo");
    }
  }
}