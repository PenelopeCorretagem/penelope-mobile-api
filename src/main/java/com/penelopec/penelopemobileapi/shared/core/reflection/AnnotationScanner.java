package com.penelopec.penelopemobileapi.shared.core.reflection;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Optional;

/**
 * Utilitário stateless para varredura de anotações em tempo de execução.
 * Facilita a busca de metadados em classes, métodos ou atributos com suporte a travessia de herança.
 */
public final class AnnotationScanner {

  private AnnotationScanner() {
    throw new UnsupportedOperationException("Utilitário stateless não deve ser instanciado.");
  }

  /**
   * Busca uma anotação numa classe. Se não encontrar, varre a hierarquia (superclasses)
   * até o topo (Object.class) antes de desistir.
   *
   * @param clazz A classe a ser inspecionada.
   * @param annotationType O tipo de anotação buscada.
   * @param <A> Tipo genérico da anotação.
   * @return Um Optional contendo a anotação, ou vazio caso não seja encontrada.
   */
  public static <A extends Annotation> Optional<A> findAnnotation(Class<?> clazz, Class<A> annotationType) {
    Objects.requireNonNull(clazz, "A classe alvo não pode ser nula");
    Objects.requireNonNull(annotationType, "O tipo da anotação não pode ser nulo");

    Class<?> current = clazz;

    while (current != null && current != Object.class) {
      if (current.isAnnotationPresent(annotationType)) {
        return Optional.of(current.getAnnotation(annotationType));
      }
      current = current.getSuperclass();
    }

    return Optional.empty();
  }

  /**
   * Busca uma meta-anotação associada a qualquer anotação presente na classe alvo.
   * Se não encontrar na classe atual, continua a varredura pela hierarquia.
   *
   * @param clazz A classe a ser inspecionada.
   * @param annotationType O tipo da meta-anotação buscada.
   * @param <A> Tipo genérico da meta-anotação.
   * @return Um Optional contendo a meta-anotação encontrada, ou vazio caso ausente.
   */
  public static <A extends Annotation> Optional<A> findMetaAnnotation(Class<?> clazz, Class<A> annotationType) {
    Objects.requireNonNull(clazz, "A classe alvo não pode ser nula");
    Objects.requireNonNull(annotationType, "O tipo da anotação não pode ser nulo");

    Class<?> current = clazz;

    while (current != null && current != Object.class) {
      for (Annotation annotation : current.getDeclaredAnnotations()) {
        A metaAnnotation = annotation.annotationType().getAnnotation(annotationType);
        if (metaAnnotation != null) {
          return Optional.of(metaAnnotation);
        }
      }
      current = current.getSuperclass();
    }

    return Optional.empty();
  }
}
