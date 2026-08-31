package com.penelopec.penelopemobileapi.shared.core.io;


import com.penelopec.penelopemobileapi.shared.core.exception.InfrastructureException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Utilitário stateless para manipulação segura de abstrações Path (NIO.2).
 * Focado na prevenção de vulnerabilidades de Path Traversal e extração de metadados.
 */
public final class PathUtils {

  private PathUtils() {
    throw new UnsupportedOperationException("Utilitário stateless não deve ser instanciado.");
  }

  /**
   * Resolve um caminho fornecido por fonte externa de forma segura, garantindo
   * que o resultado não escape do diretório base especificado.
   *
   * @param baseDir Diretório raiz onde o arquivo deve ficar confinado.
   * @param userPath Caminho ou nome do arquivo fornecido pelo utilizador.
   * @return O Path final, normalizado e seguro.
   * @throws SecurityException se for detectada tentativa de Path Traversal.
   */
  public static Path resolveSafe(Path baseDir, String userPath) {
    Objects.requireNonNull(baseDir, "PathUtils: baseDir não pode ser nulo.");
    Objects.requireNonNull(userPath, "PathUtils: userPath não pode ser nulo.");

    Path normalizedBase = baseDir.normalize();
    Path resolvedPath = normalizedBase.resolve(userPath).normalize();

    if (!resolvedPath.startsWith(normalizedBase)) {
      throw new SecurityException("Tentativa de Path Traversal detectada. Caminho inválido: " + userPath);
    }

    return resolvedPath;
  }

  /**
   * Extrai a extensão de um arquivo de forma segura (sem o ponto).
   *
   * @param path O caminho do arquivo.
   * @return Optional contendo a extensão em minúsculas, ou vazio se não houver.
   */
  public static Optional<String> getExtension(Path path) {
    Objects.requireNonNull(path, "PathUtils: path não pode ser nulo.");

    String fileName = path.getFileName().toString();
    int lastDotIndex = fileName.lastIndexOf('.');

    if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
      return Optional.of(fileName.substring(lastDotIndex + 1).toLowerCase());
    }

    return Optional.empty();
  }

  public static void ensureDirectory(Path dir) {
    Objects.requireNonNull(dir, "PathUtils: dir não pode ser nulo.");

    if (Files.exists(dir) && !Files.isDirectory(dir)) {
      throw new InfrastructureException(
        "PathUtils: o caminho existe, mas não é um diretório: " + dir
      );
    }

    if (!Files.exists(dir)) {
      try {
        Files.createDirectories(dir);
      } catch (IOException e) {
        throw new InfrastructureException(
          "PathUtils: falha ao garantir diretório: " + dir, e
        );
      }
    }
  }
}