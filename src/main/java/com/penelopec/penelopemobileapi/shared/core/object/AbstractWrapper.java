package com.penelopec.penelopemobileapi.shared.core.object;

import java.util.Objects;

/**
 * Esqueleto padrão e utilitário para implementações de {@link Wrapper}.
 * Reduz o boilerplate de composição e garante contratualmente que
 * o objeto encapsulado nunca será nulo.
 *
 * @param <T> O tipo do objeto encapsulado.
 */
public abstract class AbstractWrapper<T> implements Wrapper<T> {

  protected final T delegate;

  protected AbstractWrapper(T delegate) {
    this.delegate = Objects.requireNonNull(delegate, "O objeto delegado não pode ser nulo em uma composição.");
  }

  @Override
  public final T unwrap() {
    return delegate;
  }
}
