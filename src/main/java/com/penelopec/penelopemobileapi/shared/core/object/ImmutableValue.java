package com.penelopec.penelopemobileapi.shared.core.object;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Encapsula um valor de forma segura, favorecendo Composição em vez de Herança.
 * Esta classe é {@code final} por design, prevenindo o Fragile Base Class Problem
 * e garantindo que o comportamento de igualdade e transformação não possa ser adulterado.
 *
 * @param <T> o tipo do valor encapsulado.
 */
public final class ImmutableValue<T> {
  private final T value;

  public ImmutableValue(T value) {
    this.value = Objects.requireNonNull(value, "O valor encapsulado não pode ser nulo.");
  }

  public T get() {
    return value;
  }

  /**
   * Factory method para criar um novo ImmutableValue.
   *
   * @param value O valor a ser encapsulado.
   * @param <T> O tipo do valor.
   * @return Uma instância garantida contra extensão.
   */
  public static <T> ImmutableValue<T> of(T value) {
    return new ImmutableValue<T>(value);
  }

  /**
   * Aplica uma transformação ao valor encapsulado e retorna um novo ImmutableValue,
   * promovendo a imutabilidade durante manipulações.
   *
   * @param mapper A função de transformação.
   * @param <U> O novo tipo de dado.
   * @return Um novo ImmutableValue contendo o resultado.
   */
  public <U> ImmutableValue<U> map(Function<? super T, ? extends U> mapper) {
    Objects.requireNonNull(mapper, "Mapper não pode ser nulo.");
    return ImmutableValue.of(mapper.apply(value));
  }

  /**
   * Executa uma ação (efeito colateral) utilizando o valor interno,
   * sem alterá-lo, e retorna a própria instância atual.
   * * Este método é ideal para operações de auditoria, logs de debug,
   * ou envio de métricas no meio de uma cadeia de operações (fluent API),
   * garantindo que o fluxo não seja interrompido.
   *
   * @param consumer A ação a ser executada com o valor encapsulado.
   * @return Esta mesma instância de {@code ImmutableValue}.
   * @throws NullPointerException se o {@code consumer} fornecido for nulo.
   */
  public ImmutableValue<T> peek(Consumer<? super T> consumer) {
    Objects.requireNonNull(consumer, "Consumer não pode ser nulo.");
    consumer.accept(value);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if  (this == o) return true;
    if (!(o instanceof ImmutableValue<?> that)) return false;
    return ObjectUtils.deepEquals(value, that.value);
  }

  @Override
  public int hashCode() {
    return ObjectUtils.deepHash(value);
  }
}