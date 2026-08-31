package com.penelopec.penelopemobileapi.shared.core.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileHelper")
class FileHelperTest {

  @TempDir
  Path tempDir; // Gerenciamento nativo de pasta temporária do JUnit 5

  @Nested
  @DisplayName("Quando processa cópias de alta performance")
  class QuandoCopiaArquivos {

    @Test
    @DisplayName("Deve copiar o conteúdo integralmente preservando integridade (Zero-Copy)")
    void shouldCopyFileSuccessfullyUsingZeroCopy() throws Exception {
      Path source = tempDir.resolve("dump-origem.txt");
      Files.writeString(source, "CONTEUDO_MASSIVO_SIMULADO");

      Path target = tempDir.resolve("dump-destino.txt");

      FileHelper.fastCopy(source, target);

      assertThat(Files.readString(target)).isEqualTo("CONTEUDO_MASSIVO_SIMULADO");
    }

    @Test
    @DisplayName("Deve notificar o progresso acumulado a cada faixa configurada de bytes")
    void shouldNotifyProgressDuringCopy() throws Exception {
      Path source = tempDir.resolve("dump-progresso-origem.txt");
      Files.writeString(source, "0123456789");

      Path target = tempDir.resolve("dump-progresso-destino.txt");
      List<Long> processedBytesNotifications = new ArrayList<>();
      List<Long> totalBytesNotifications = new ArrayList<>();

      FileHelper.fastCopy(source, target, (processedBytes, totalBytes) -> {
        processedBytesNotifications.add(processedBytes);
        totalBytesNotifications.add(totalBytes);
      }, 4);

      assertThat(Files.readString(target)).isEqualTo("0123456789");
      assertThat(processedBytesNotifications).isNotEmpty();
      assertThat(processedBytesNotifications.get(processedBytesNotifications.size() - 1)).isEqualTo(10L);
      assertThat(totalBytesNotifications).allMatch(total -> total == 10L);
    }
  }

  @Nested
  @DisplayName("Quando parâmetros são mal formados")
  class QuandoParametrosMalFormados {

    @Test
    @DisplayName("Deve falhar rápido se os caminhos forem nulos")
    void shouldFailFastWhenPathsAreNull() {
      Path validPath = tempDir.resolve("dummy.txt");

      assertThatThrownBy(() -> FileHelper.fastCopy(null, validPath))
        .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção não checada quando arquivo de origem não existir")
    void shouldThrowUncheckedIOExceptionWhenSourceDoesNotExist() {
      Path ghostSource = tempDir.resolve("fantasma.bak");
      Path target = tempDir.resolve("destino.bak");

      assertThatThrownBy(() -> FileHelper.fastCopy(ghostSource, target))
        .isInstanceOf(UncheckedIOException.class)
        .hasMessageContaining("Falha de infraestrutura");
    }

    @Test
    @DisplayName("Deve falhar rápido se o listener de progresso for nulo")
    void shouldFailFastWhenProgressListenerIsNull() {
      Path source = tempDir.resolve("origem.txt");
      Path target = tempDir.resolve("destino.txt");

      assertThatThrownBy(() -> FileHelper.fastCopy(source, target, null, 1024))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("listener de progresso");
    }

    @Test
    @DisplayName("Deve falhar rapidamente quando o intervalo de notificação for inválido")
    void shouldFailFastWhenNotifyEveryBytesIsInvalid() {
      Path source = tempDir.resolve("origem.txt");
      Path target = tempDir.resolve("destino.txt");

      assertThatThrownBy(() -> FileHelper.fastCopy(source, target, (processedBytes, totalBytes) -> { }, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("intervalo de notificação");
    }
  }
}