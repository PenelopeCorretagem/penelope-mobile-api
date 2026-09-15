package com.penelopec.penelopemobileapi.shared.web.error;

import com.penelopec.penelopemobileapi.shared.core.exception.DomainError;
import com.penelopec.penelopemobileapi.shared.core.exception.NotFoundException;
import com.penelopec.penelopemobileapi.shared.core.exception.ValidationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

  private final MockMvc mockMvc = MockMvcBuilders
    .standaloneSetup(new FailingController())
    .setControllerAdvice(new GlobalExceptionHandler())
    .build();

  @Test
  void shouldMapValidationExceptionToBadRequest() throws Exception {
    mockMvc.perform(get("/test/validation").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("INPUT_INVALID"))
      .andExpect(jsonPath("$.message").value("Entrada inválida."))
      .andExpect(jsonPath("$.path").value("/test/validation"));
  }

  @Test
  void shouldMapNotFoundExceptionToNotFound() throws Exception {
    mockMvc.perform(get("/test/not-found").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("ESTATE_NOT_FOUND"));
  }

  @Test
  void shouldHideUnexpectedExceptionDetails() throws Exception {
    mockMvc.perform(get("/test/unexpected").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isInternalServerError())
      .andExpect(jsonPath("$.code").value("CORE_500"))
      .andExpect(jsonPath("$.message").value("Ocorreu um erro interno inesperado na operação."));
  }

  @Test
  void shouldReturnFieldViolationsForBeanValidationErrors() throws Exception {
    mockMvc.perform(post("/test/validation")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"name\":\"\"}"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("CORE_400"))
      .andExpect(jsonPath("$.violations[0].field").value("name"))
      .andExpect(jsonPath("$.violations[0].message").value("Nome é obrigatório."));
  }

  @RestController
  static class FailingController {

    @GetMapping("/test/validation")
    void validation() {
      throw new ValidationException(DomainError.of("INPUT_INVALID", "Entrada inválida."));
    }

    @GetMapping("/test/not-found")
    void notFound() {
      throw new NotFoundException(DomainError.of("ESTATE_NOT_FOUND", "Imóvel não encontrado."));
    }

    @GetMapping("/test/unexpected")
    void unexpected() {
      throw new IllegalStateException("Informação interna sensível");
    }

    @PostMapping("/test/validation")
    void beanValidation(@Valid @RequestBody Input input) {
      // Endpoint de teste: a validação ocorre antes da execução do método.
    }
  }

  record Input(@NotBlank(message = "Nome é obrigatório.") String name) {
  }
}
