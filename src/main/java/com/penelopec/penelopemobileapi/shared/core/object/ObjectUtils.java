package com.penelopec.penelopemobileapi.shared.core.object;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

/**
 * Utilitário de alta performance para operações fundamentais de objetos.
 * Resolve os problemas do {@link java.util.Objects} ao lidar com arrays mutáveis
 * encapsulados em Value Objects, garantindo comparações estruturais precisas.
 */
public final class ObjectUtils {

  private ObjectUtils() {
    throw new UnsupportedOperationException("Classe utilitária não deve ser instanciada.");
  }

  /**
   * Verifica a igualdade profunda entre dois objetos, tratando arrays de forma transparente.
   * Evita NullPointerException e resolve o problema de comparação de ponteiros em arrays.
   *
   * @param a O primeiro objeto (pode ser null).
   * @param b O segundo objeto (pode ser null).
   * @return true se forem estruturalmente iguais, false caso contrário.
   */
  public static boolean deepEquals(Object a, Object b) {
    if (a == b) {
      return true;
    }
    if (a == null || b == null || a.getClass() != b.getClass()) {
      return false;
    }
    if (a.getClass().isArray()) {
      return Arrays.deepEquals(new Object[]{a}, new Object[]{b});
    }
    return Objects.equals(a, b);
  }

  /**
   * Gera um código de hash consistente que suporta arrays transparentemente.
   *
   * @param values Os objetos para os quais calcular o hash.
   * @return O hash code calculado.
   */
  public static int deepHash(Object... values) {
    return Arrays.deepHashCode(values);
  }

  /**
   * Inspeciona a classe fornecida e garante que todos os seus atributos sejam 'final'.
   *
   * @param clazz A classe a ser inspecionada.
   * @throws IllegalArgumentException se a classe contiver atributos não-final.
   */
  public static void requireImmutable(Class<?> clazz) {
    if (clazz == null) throw new IllegalArgumentException("A classe fornecida não pode ser nula.");

    StringBuilder message = new StringBuilder(String.format("A classe %s não é estritamente imutável. \n", clazz.getName()));
    boolean found = false;

    for (Field field : clazz.getDeclaredFields()) {
      if (field.isSynthetic()) {
        continue;
      }

      if (!Modifier.isFinal(field.getModifiers())) {
        found = true;
        message.append(String.format("O atributo '%s' deve ser declarado como final. \n", field.getName()));
      }
    }

    if (found) throw new IllegalArgumentException(message.toString());
  }
}