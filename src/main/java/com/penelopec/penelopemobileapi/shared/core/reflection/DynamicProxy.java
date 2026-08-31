package com.penelopec.penelopemobileapi.shared.core.reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * Utilitário stateless para criação simplificada e segura de Proxies Dinâmicos.
 */
public final class DynamicProxy {

  private static final Set<String> MUTATING_LIST_METHOD_SIGNATURES = Set.of(
    "add#1",
    "add#2",
    "addAll#1",
    "addAll#2",
    "clear#0",
    "remove#1",
    "removeAll#1",
    "retainAll#1",
    "replaceAll#1",
    "sort#1",
    "set#2",
    "removeIf#1"
  );

  private DynamicProxy() {
    throw new UnsupportedOperationException("Utilitário stateless não deve ser instanciado.");
  }

  /**
   * Cria um proxy dinâmico em torno de uma interface para interceptação de métodos.
   *
   * @param targetInterface A interface que o proxy deve implementar.
   * @param handler A lógica de interceptação a ser executada.
   * @param <T> O tipo da interface.
   * @return Uma instância dinamicamente gerada que implementa a interface alvo.
   */
  @SuppressWarnings("unchecked")
  public static <T> T create(Class<T> targetInterface, InvocationHandler handler) {
    Objects.requireNonNull(targetInterface, "A interface alvo não pode ser nula");
    Objects.requireNonNull(handler, "O InvocationHandler não pode ser nulo");

    if (!targetInterface.isInterface()) {
      throw new IllegalArgumentException("DynamicProxy apenas suporta interfaces. Tipo fornecido: " + targetInterface.getName());
    }

    return (T) Proxy.newProxyInstance(
      targetInterface.getClassLoader(),
      new Class<?>[]{targetInterface},
      handler
    );
  }

  /**
   * Cria uma visualização não modificável de uma lista usando Proxy Dinâmico.
   * Qualquer tentativa de mutação gera UnsupportedOperationException,
   * preservando semântica equivalente ao Collections.unmodifiableList().
   *
   * @param source a lista de origem
   * @param <E> tipo dos elementos
   * @return proxy da lista de origem com mutações bloqueadas
   */
  public static <E> List<E> unmodifiableList(List<E> source) {
    Objects.requireNonNull(source, "A lista de origem não pode ser nula");

    InvocationHandler handler = (proxy, method, args) -> {
      if (isMutatingListMethod(method)) {
        throw new UnsupportedOperationException("Esta lista é somente leitura.");
      }

      Object result = invoke(method, source, args);

      if ("iterator".equals(method.getName()) && result instanceof Iterator<?> iterator) {
        return unmodifiableIterator(iterator);
      }

      if ("listIterator".equals(method.getName()) && result instanceof ListIterator<?> listIterator) {
        return unmodifiableListIterator(listIterator);
      }

      if ("subList".equals(method.getName()) && result instanceof List<?> subList) {
        @SuppressWarnings("unchecked")
        List<E> typedSubList = (List<E>) subList;
        return unmodifiableList(typedSubList);
      }

      return result;
    };

    return create(List.class, handler);
  }

  private static boolean isMutatingListMethod(Method method) {
    String signature = method.getName() + "#" + method.getParameterCount();
    return MUTATING_LIST_METHOD_SIGNATURES.contains(signature);
  }

  private static Object invoke(Method method, Object target, Object[] args) throws Throwable {
    try {
      return method.invoke(target, args);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  @SuppressWarnings("unchecked")
  private static <E> Iterator<E> unmodifiableIterator(Iterator<E> iterator) {
    InvocationHandler iteratorHandler = (proxy, method, args) -> {
      if ("remove".equals(method.getName())) {
        throw new UnsupportedOperationException("Esta lista é somente leitura.");
      }
      return invoke(method, iterator, args);
    };

    return create(Iterator.class, iteratorHandler);
  }

  @SuppressWarnings("unchecked")
  private static <E> ListIterator<E> unmodifiableListIterator(ListIterator<E> listIterator) {
    InvocationHandler listIteratorHandler = (proxy, method, args) -> {
      if ("remove".equals(method.getName()) || "set".equals(method.getName()) || "add".equals(method.getName())) {
        throw new UnsupportedOperationException("Esta lista é somente leitura.");
      }
      return invoke(method, listIterator, args);
    };

    return create(ListIterator.class, listIteratorHandler);
  }
}
