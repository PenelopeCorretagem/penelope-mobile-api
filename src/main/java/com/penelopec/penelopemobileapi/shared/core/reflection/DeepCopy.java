package com.penelopec.penelopemobileapi.shared.core.reflection;

import sun.misc.Unsafe;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Utilitário para clonagem profunda e recursiva de objetos.
 */
public final class DeepCopy {

  private static final Unsafe UNSAFE = resolveUnsafe();

  private DeepCopy() { throw new UnsupportedOperationException(); }

  /**
   * Executa uma cópia profunda do objeto, resolvendo referências circulares.
   */
  @SuppressWarnings("unchecked")
  public static <T> T of(T source) {
    return (T) cloneRecursive(source, new IdentityHashMap<>());
  }

  private static Object cloneRecursive(Object source, Map<Object, Object> visited) {
    if (source == null) return null;

    // Tipos primitivos, Wrappers e Strings são imutáveis e passados por valor/referência constante
    Class<?> clazz = source.getClass();
    if (clazz.isPrimitive() || clazz == String.class || Number.class.isAssignableFrom(clazz) || clazz == Boolean.class) {
      return source;
    }

    // Se já visitamos esse objeto, retorna a instância já clonada (prevenção de Circular Reference)
    if (visited.containsKey(source)) {
      return visited.get(source);
    }

    // Tratamento especial para Arrays
    if (clazz.isArray()) {
      int length = Array.getLength(source);
      Object arrayClone = Array.newInstance(clazz.getComponentType(), length);
      visited.put(source, arrayClone);
      for (int i = 0; i < length; i++) {
        Array.set(arrayClone, i, cloneRecursive(Array.get(source, i), visited));
      }
      return arrayClone;
    }

    // Instancia novo POJO
    try {
      Object clone = instantiateWithoutConstructorRequirement(clazz);

      visited.put(source, clone);

      // Copia recursivamente os campos
      Class<?> current = clazz;
      while (current != null && current != Object.class) {
        for (Field field : current.getDeclaredFields()) {
          if (Modifier.isStatic(field.getModifiers())) continue;

          boolean originalAccess = field.canAccess(clone);
          if (!originalAccess) field.setAccessible(true);

          Object fieldValue = field.get(source);
          Object clonedFieldValue = cloneRecursive(fieldValue, visited);

          // Tratamento forçado para ignorar final em reflexão profunda (apenas didático, evite em produção se possível)
          field.set(clone, clonedFieldValue);

          if (!originalAccess) field.setAccessible(false);
        }
        current = current.getSuperclass();
      }
      return clone;

    } catch (Exception e) {
      throw new RuntimeException("Falha ao realizar Deep Copy da classe " + clazz.getName() + ".", e);
    }
  }

  private static Object instantiateWithoutConstructorRequirement(Class<?> clazz) throws Exception {
    try {
      Constructor<?> constructor = clazz.getDeclaredConstructor();
      boolean originalConstructorAccess = constructor.canAccess(null);
      try {
        if (!originalConstructorAccess) {
          constructor.setAccessible(true);
        }
        return constructor.newInstance();
      } finally {
        if (!originalConstructorAccess) {
          constructor.setAccessible(false);
        }
      }
    } catch (NoSuchMethodException e) {
      // Fallback para tipos sem construtor vazio.
      return UNSAFE.allocateInstance(clazz);
    }
  }

  private static Unsafe resolveUnsafe() {
    try {
      Field field = Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      return (Unsafe) field.get(null);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Não foi possível inicializar o acesso ao Unsafe para DeepCopy.", e);
    }
  }
}