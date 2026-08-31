package com.penelopec.penelopemobileapi.shared.core.jvm;

import java.util.Objects;
import java.util.Optional;

/**
 * Utilitário stateless para operações seguras de Class Loading.
 * <p>
 * Resolve a complexidade de obtenção do ClassLoader correto em ambientes
 * conteinerizados (Web, OSGi) e transforma as APIs reflexivas arcaicas e
 * falhas (baseadas em exceções checadas) em fluxos funcionais.
 */
public final class ClassLoadingUtils {

  private ClassLoadingUtils() {
  }

  /**
   * Tenta carregar uma classe de forma segura e silenciosa, sem disparar exceções no fluxo de negócio.
   * Útil para auto-configurações condicionais (ex: "se a classe X existir, ative a feature Y").
   *
   * @param fullyQualifiedClassName O nome completo da classe (ex: "java.lang.String").
   * @return Um Optional contendo a Classe se encontrada, ou vazio caso não exista no classpath.
   */
  public static Optional<Class<?>> loadClassSilently(String fullyQualifiedClassName) {
    Objects.requireNonNull(fullyQualifiedClassName, "O nome da classe não pode ser nulo");

    try {
      return Optional.of(Class.forName(fullyQualifiedClassName, false, getDefaultClassLoader()));
    } catch (ClassNotFoundException | LinkageError e) {
      // LinkageError captura cenários severos como NoClassDefFoundError sem quebrar o utilitário
      return Optional.empty();
    }
  }

  /**
   * Verifica de forma segura se uma classe está acessível no classpath.
   * Reuusa a infraestrutura de {@link #loadClassSilently(String)}, retornando apenas
   * o resultado booleano sem necessidade de desembrulhar o Optional.
   *
   * @param fullyQualifiedClassName O nome completo da classe (ex: "java.lang.String").
   * @return true se a classe estiver acessível no classpath, false caso contrário.
   */
  public static boolean isPresent(String fullyQualifiedClassName) {
    return loadClassSilently(fullyQualifiedClassName).isPresent();
  }

  /**
   * Obtém o ClassLoader mais apropriado para o contexto atual de execução.
   * <p>
   * Em servidores de aplicação, a Thread atual (ContextClassLoader) possui a visibilidade
   * correta das classes da aplicação Web, enquanto o ClassLoader do sistema veria apenas
   * as classes do servidor em si.
   *
   * @return O ClassLoader ativo, nunca nulo.
   */
  public static ClassLoader getDefaultClassLoader() {
    ClassLoader cl = null;

    try {
      cl = Thread.currentThread().getContextClassLoader();
    } catch (Throwable ex) {
      // Ignora falhas de segurança no acesso ao ContextClassLoader
    }

    if (cl == null) {
      cl = ClassLoadingUtils.class.getClassLoader();
      if (cl == null) {
        // Em último caso, tenta o ClassLoader do Sistema (pode ser null se a classe
        // chamadora foi carregada pelo Bootstrap ClassLoader)
        try {
          cl = ClassLoader.getSystemClassLoader();
        } catch (Throwable ex) {
          // Retorno falho
        }
      }
    }
    return cl;
  }
}
