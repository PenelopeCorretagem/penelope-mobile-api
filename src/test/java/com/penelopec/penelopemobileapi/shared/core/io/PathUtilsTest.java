package com.penelopec.penelopemobileapi.shared.core.io;

import com.penelopec.penelopemobileapi.shared.core.exception.InfrastructureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PathUtils")
class PathUtilsTest {

  @TempDir
  Path tempDir;

  @Nested
  @DisplayName("Quando resolvemos caminhos seguros")
  class QuandoResolveCaminhos {

    @Test
    @DisplayName("Deve resolver arquivo perfeitamente confinado no diretório base")
    void shouldResolveSuccessfullyWhenPathIsConfined() {
      Path baseDir = Path.of("/var/uploads");
      Path result = PathUtils.resolveSafe(baseDir, "documento.pdf");

      assertThat(result).isEqualTo(Path.of("/var/uploads/documento.pdf"));
    }

    @Test
    @DisplayName("Deve rejeitar tentativa de Path Traversal com SecurityException")
    void shouldThrowSecurityExceptionWhenPathEscapesBaseDir() {
      Path baseDir = Path.of("/var/uploads");

      assertThatThrownBy(() -> PathUtils.resolveSafe(baseDir, "../../etc/passwd"))
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("Tentativa de Path Traversal detectada");
    }
  }

  @Nested
  @DisplayName("Quando extraímos extensões")
  class QuandoExtraiExtensao {

    @Test
    @DisplayName("Deve extrair a extensão corretamente ignorando dots iniciais (arquivos ocultos Linux)")
    void shouldExtractExtensionIgnoringHiddenFileDot() {
      Path path = Path.of("/home/user/.bash_profile.bak");

      assertThat(PathUtils.getExtension(path))
        .isPresent()
        .contains("bak");
    }
  }

  @Nested
  @DisplayName("Quando garantimos diretórios")
  class QuandoGaranteDiretorio {

    @Test
    @DisplayName("Deve criar diretórios pais recursivamente quando não existirem")
    void shouldCreateDirectoriesRecursively() {
      Path target = tempDir.resolve("a").resolve("b").resolve("c");

      PathUtils.ensureDirectory(target);

      assertThat(Files.exists(target)).isTrue();
      assertThat(Files.isDirectory(target)).isTrue();
    }

    @Test
    @DisplayName("Não deve falhar quando o diretório já existir")
    void shouldNotFailWhenDirectoryAlreadyExists() {
      Path existingDir = tempDir.resolve("already-exists");
      PathUtils.ensureDirectory(existingDir);

      assertThatCode(() -> PathUtils.ensureDirectory(existingDir))
        .doesNotThrowAnyException();
      assertThat(Files.isDirectory(existingDir)).isTrue();
    }

    @Test
    @DisplayName("Deve lançar exceção de infraestrutura quando o caminho já existir como arquivo")
    void shouldThrowInfrastructureExceptionWhenPathExistsAsRegularFile() throws Exception {
      Path regularFile = tempDir.resolve("arquivo.txt");
      Files.writeString(regularFile, "conteudo");

      assertThatThrownBy(() -> PathUtils.ensureDirectory(regularFile))
        .isInstanceOf(InfrastructureException.class)
        .hasMessageContaining("não é um diretório");
    }

    @Test
    @DisplayName("Deve falhar rapidamente quando dir for nulo")
    void shouldFailFastWhenDirIsNull() {
      assertThatThrownBy(() -> PathUtils.ensureDirectory(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("dir não pode ser nulo");
    }
  }
}