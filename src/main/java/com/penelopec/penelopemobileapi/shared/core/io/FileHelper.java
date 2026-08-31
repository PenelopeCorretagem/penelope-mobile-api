package com.penelopec.penelopemobileapi.shared.core.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Utilitário stateless para operações pesadas de I/O em arquivos.
 * Utiliza o poder do NIO.2 e Channels para aplicar padrões de alta
 * performance como o Zero-Copy da JVM.
 */
public final class FileHelper {

  @FunctionalInterface
  public interface ProgressListener {
    /**
     * Recebe o progresso acumulado da cópia.
     *
     * @param processedBytes quantidade de bytes já copiados
     * @param totalBytes quantidade total de bytes do arquivo de origem
     */
    void onProgress(long processedBytes, long totalBytes);
  }

  private FileHelper() {
    throw new UnsupportedOperationException("Utilitário stateless não deve ser instanciado.");
  }

  /**
   * Copia um arquivo de forma ultra-rápida utilizando DMA (Zero-Copy).
   * Ideal para arquivos massivos (> 100MB, GBs, TBs), pois os bytes trafegam
   * no Kernel do SO sem transitar pelo Heap da JVM.
   *
   * @param source Caminho do arquivo de origem.
   * @param target Caminho do arquivo de destino (será substituído se existir).
   * @throws UncheckedIOException se ocorrer um erro de I/O na máquina física.
   */
  public static void fastCopy(Path source, Path target) {
    try (FileChannel sourceChannel = FileChannel.open(
      Objects.requireNonNull(source, "FileHelper: O arquivo de origem não pode ser nulo."),
      StandardOpenOption.READ
    ); FileChannel targetChannel = FileChannel.open(
      Objects.requireNonNull(target, "FileHelper: O arquivo de destino não pode ser nulo."),
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE,
      StandardOpenOption.TRUNCATE_EXISTING
    )) {

      long size = sourceChannel.size();
      long position = 0;

      // O loop é necessário caso o SO tenha limitações no tamanho máximo de transferência atômica
      while (position < size) {
        long transferred = sourceChannel.transferTo(position, size - position, targetChannel);
        if (transferred <= 0) {
          throw new IOException("Nenhum byte foi transferido durante a cópia Zero-Copy.");
        }
        position += transferred;
      }

    } catch (IOException e) {
      throw new UncheckedIOException("Falha de infraestrutura durante a cópia Zero-Copy do arquivo.", e);
    }
  }

  /**
   * Copia um arquivo de forma ultra-rápida utilizando DMA (Zero-Copy),
   * notificando o progresso acumulado a cada faixa configurada de bytes.
   *
   * @param source Caminho do arquivo de origem.
   * @param target Caminho do arquivo de destino (será substituído se existir).
   * @param progressListener callback acionado conforme o avanço da cópia.
   * @param notifyEveryBytes intervalo mínimo, em bytes, entre notificações sucessivas.
   * @throws IllegalArgumentException se notifyEveryBytes for menor ou igual a zero.
   * @throws UncheckedIOException se ocorrer um erro de I/O na máquina física.
   */
  public static void fastCopy(Path source, Path target, ProgressListener progressListener, long notifyEveryBytes) {
    Objects.requireNonNull(source, "FileHelper: O arquivo de origem não pode ser nulo.");
    Objects.requireNonNull(target, "FileHelper: O arquivo de destino não pode ser nulo.");
    Objects.requireNonNull(progressListener, "FileHelper: O listener de progresso não pode ser nulo.");

    if (notifyEveryBytes <= 0) {
      throw new IllegalArgumentException("FileHelper: O intervalo de notificação deve ser maior que zero.");
    }

    try (FileChannel sourceChannel = FileChannel.open(source, StandardOpenOption.READ);
         FileChannel targetChannel = FileChannel.open(target,
           StandardOpenOption.CREATE,
           StandardOpenOption.WRITE,
           StandardOpenOption.TRUNCATE_EXISTING)) {

      long size = sourceChannel.size();
      long position = 0;
      long nextNotificationBoundary = Math.min(notifyEveryBytes, size);

      // O loop é necessário caso o SO tenha limitações no tamanho máximo de transferência atômica
      while (position < size) {
        long transferred = sourceChannel.transferTo(position, size - position, targetChannel);
        if (transferred <= 0) {
          throw new IOException("Nenhum byte foi transferido durante a cópia Zero-Copy.");
        }

        position += transferred;

        while (position >= nextNotificationBoundary && nextNotificationBoundary > 0) {
          progressListener.onProgress(Math.min(position, size), size);
          if (nextNotificationBoundary == size) {
            break;
          }
          nextNotificationBoundary = Math.min(nextNotificationBoundary + notifyEveryBytes, size);
        }
      }

      if (size == 0) {
        progressListener.onProgress(0, 0);
      }

    } catch (IOException e) {
      throw new UncheckedIOException("Falha de infraestrutura durante a cópia Zero-Copy do arquivo.", e);
    }
  }
}
