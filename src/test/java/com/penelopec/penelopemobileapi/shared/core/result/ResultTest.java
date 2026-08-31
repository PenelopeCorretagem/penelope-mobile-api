package com.penelopec.penelopemobileapi.shared.core.result;

import com.penelopec.penelopemobileapi.shared.core.exception.DomainError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Result")
class ResultTest {

  @Nested
  @DisplayName("Quando instanciado")
  class Instantiation {

    @Test
    @DisplayName("Deve criar Success com valor")
    void shouldCreateSuccessWithValue() {
      Result<String, DomainError> result = Result.success("OK");
      assertThat(result).isInstanceOf(Result.Success.class);
    }

    @Test
    @DisplayName("Deve criar Success sem valor (Void)")
    void shouldCreateSuccessWithVoid() {
      Result<Void, DomainError> result = Result.success();
      assertThat(result).isInstanceOf(Result.Success.class);
      assertThat(((Result.Success<Void, DomainError>) result).value()).isEqualTo(Void.singleton());
    }

    @Test
    @DisplayName("Deve criar Failure")
    void shouldCreateFailure() {
      Result<String, DomainError> result = Result.failure(DomainError.of("ERR", "Falhou"));
      assertThat(result).isInstanceOf(Result.Failure.class);
    }
  }

  @Nested
  @DisplayName("Quando transformado via map e mapError")
  class Mapping {

    @Test
    @DisplayName("Deve transformar Success")
    void shouldTransformSuccess() {
      Result<String, DomainError> result = Result.<Integer, DomainError>success(10).map(String::valueOf);
      assertThat(((Result.Success<String, DomainError>) result).value()).isEqualTo("10");
    }

    @Test
    @DisplayName("Deve propagar Failure no map")
    void shouldPropagateFailure() {
      DomainError error = DomainError.of("ERR", "Erro");
      Result<Integer, DomainError> result = Result.failure(error);
      Result<String, DomainError> mapped = result.map(String::valueOf);

      assertThat(mapped).isInstanceOf(Result.Failure.class);
      assertThat(((Result.Failure<?, DomainError>) mapped).error()).isEqualTo(error);
    }

    @Test
    @DisplayName("Deve mapear o erro no mapError se for Failure")
    void shouldMapErrorOnFailure() {
      Result<String, String> result = Result.<String, DomainError>failure(DomainError.of("400", "Bad Request"))
        .mapError(DomainError::message);

      assertThat(((Result.Failure<String, String>) result).error()).isEqualTo("Bad Request");
    }
  }

  @Nested
  @DisplayName("Quando disparar efeitos colaterais (Side-Effects)")
  class SideEffects {

    @Test
    @DisplayName("Deve executar acao de sucesso se for Success")
    void shouldExecuteOnSuccessAction() {
      AtomicBoolean successCalled = new AtomicBoolean(false);
      AtomicBoolean failureCalled = new AtomicBoolean(false);

      Result.success("Dados")
        .onSuccess(v -> successCalled.set(true))
        .onFailure(e -> failureCalled.set(true));

      assertThat(successCalled.get()).isTrue();
      assertThat(failureCalled.get()).isFalse();
    }

    @Test
    @DisplayName("Deve executar acao de falha se for Failure")
    void shouldExecuteOnFailureAction() {
      AtomicReference<String> errorMsg = new AtomicReference<>("");

      Result.<String, DomainError>failure(DomainError.of("500", "Timeout"))
        .onSuccess(v -> {})
        .onFailure(e -> errorMsg.set(e.message()));

      assertThat(errorMsg.get()).isEqualTo("Timeout");
    }
  }

  @Nested
  @DisplayName("Quando usar fold")
  class Folding {

    @Test
    @DisplayName("Deve executar fluxo de sucesso")
    void shouldExecuteSuccessBranch() {
      String result = Result.<Integer, DomainError>success(10).fold(String::valueOf, DomainError::message);
      assertThat(result).isEqualTo("10");
    }

    @Test
    @DisplayName("Deve executar fluxo de falha")
    void shouldExecuteFailureBranch() {
      String result = Result.<Integer, DomainError>failure(DomainError.of("ERR", "Falhou"))
        .fold(String::valueOf, DomainError::message);
      assertThat(result).isEqualTo("Falhou");
    }
  }

  @Nested
  @DisplayName("Quando utilizar pontes de interoperabilidade")
  class InteroperabilityCases {

    @Test
    @DisplayName("Deve extrair o valor via orElseThrow se for Success")
    void shouldExtractValueWithOrElseThrowOnSuccess() {
      Result<String, DomainError> result = Result.success("Dados vitais");

      String value = result.orElseThrow(err -> new RuntimeException("Não deve lançar"));

      assertThat(value).isEqualTo("Dados vitais");
    }

    @Test
    @DisplayName("Deve lançar a exceção fornecida via orElseThrow se for Failure")
    void shouldThrowExceptionWithOrElseThrowOnFailure() {
      Result<String, DomainError> result = Result.failure(DomainError.of("404", "Não encontrado"));

      assertThatThrownBy(() -> result.orElseThrow(err -> new IllegalArgumentException(err.message())))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Não encontrado");
    }

    @Test
    @DisplayName("Deve converter para Optional preenchido se for Success")
    void shouldConvertToOptionalOnSuccess() {
      Result<String, DomainError> result = Result.success("Valor Presente");

      Optional<String> optional = result.toOptional();

      assertThat(optional).isPresent().contains("Valor Presente");
    }

    @Test
    @DisplayName("Deve converter para Optional vazio (silenciando o erro) se for Failure")
    void shouldConvertToEmptyOptionalOnFailure() {
      Result<String, DomainError> result = Result.failure(DomainError.of("500", "Erro Fatal"));

      Optional<String> optional = result.toOptional();

      assertThat(optional).isEmpty();
    }

    @Test
    @DisplayName("Deve ignorar função recover e retornar a própria instância se for Success")
    void shouldIgnoreRecoverOnSuccess() {
      Result<String, DomainError> result = Result.success("Original");

      Result<String, DomainError> recovered = result.recover(err -> "Fallback");

      assertThat(((Result.Success<String, DomainError>) recovered).value()).isEqualTo("Original");
    }

    @Test
    @DisplayName("Deve gerar um novo Success baseado no erro ao chamar recover se for Failure")
    void shouldRecoverAndReturnNewSuccessOnFailure() {
      Result<String, DomainError> result = Result.failure(DomainError.of("CACHE_MISS", "Dado não encontrado"));

      Result<String, DomainError> recovered = result.recover(err -> "Dado Padrão");

      assertThat(recovered).isInstanceOf(Result.Success.class);
      assertThat(((Result.Success<String, DomainError>) recovered).value()).isEqualTo("Dado Padrão");
    }
  }
}