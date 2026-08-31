package com.penelopec.penelopemobileapi.shared.core.io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Utilitário stateless para carregamento atômico de recursos do classpath.
 * Garante o fechamento de fluxos de I/O e assegura a codificação UTF-8 por padrão,
 * evitando anomalias de formatação em ambientes distintos (Mojibake).
 */
public final class ResourceLoader {

  private ResourceLoader() {
    throw new UnsupportedOperationException("Utilitário stateless não deve ser instanciado.");
  }

  /**
   * Lê todo o conteúdo de um arquivo do classpath para uma String.
   *
   * @param path O caminho do arquivo (ex: "templates/email.html").
   * @return O conteúdo do arquivo lido em UTF-8.
   * @throws IllegalArgumentException se o arquivo não for encontrado.
   * @throws UncheckedIOException se ocorrer um erro de I/O durante a leitura.
   */
  public static String readAsString(String path) {
    Objects.requireNonNull(path, "ResourceLoader: path não pode ser nulo.");

    // Remove a barra inicial para unificar a resolução do ClassLoader
    String normalizedPath = path.startsWith("/") ? path.substring(1) : path;

    // O try-with-resources garante o fechamento automático do InputStream (invoca .close())
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream is = classLoader.getResourceAsStream(normalizedPath)) {
      if (is == null) {
        throw new IllegalArgumentException("Recurso não encontrado no classpath: " + normalizedPath);
      }

      // Lê atomicamente os bytes delegando o buffer interno para a JVM e traduz via UTF-8
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);

    } catch (IOException e) {
      throw new UncheckedIOException("Erro de I/O ao processar o recurso: " + normalizedPath, e);
    }
  }

  public static Stream<String> readAsLines(String path) {
    String normalizedPath = path.startsWith("/") ? path.substring(1) : path;

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream is = classLoader.getResourceAsStream(normalizedPath);
    if (is == null) {
      throw new IllegalArgumentException("Recurso não encontrado no classpath: " + normalizedPath);
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

    return reader.lines().onClose(() -> {
      try {
        reader.close();
      } catch (IOException e) {
        throw new UncheckedIOException("Erro ao fechar o recurso: " + normalizedPath, e);
      }
    });
  }

}