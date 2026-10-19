package com.penelopec.penelopemobileapi.search;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.penelopec.penelopemobileapi.shared.web.error.GlobalExceptionHandler;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@DisplayName("API REST de pesquisa de imóveis")
class PropertySearchControllerTest {
    private PropertySearchInterpretationService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(PropertySearchInterpretationService.class);
        mockMvc = MockMvcBuilders
            .standaloneSetup(new PropertySearchController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("Deve retornar os filtros interpretados no contrato da API")
    void shouldReturnInterpretedFilters() throws Exception {
        when(service.interpret(any())).thenReturn(new PropertySearchFilters(
            "apartamento",
            "São Paulo",
            null,
            "TODOS",
            2,
            new BigDecimal("500000"),
            "distance"
        ));

        mockMvc.perform(post("/v1/property-search/interpret")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "transcript": "Apartamento com dois quartos até 500 mil",
                      "cities": ["São Paulo"],
                      "regions": ["Centro"],
                      "propertyTypes": ["TODOS", "LANCAMENTO", "DISPONIVEL", "EM_OBRAS"]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.searchTerm").value("apartamento"))
            .andExpect(jsonPath("$.city").value("São Paulo"))
            .andExpect(jsonPath("$.minBedrooms").value(2))
            .andExpect(jsonPath("$.maxPrice").value(500000));

        verify(service).interpret(any());
    }

    @Test
    @DisplayName("Deve usar a resposta estruturada padrão para erros de validação")
    void shouldReturnStandardValidationError() throws Exception {
        mockMvc.perform(post("/v1/property-search/interpret")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "transcript": " ",
                      "cities": [],
                      "regions": [],
                      "propertyTypes": ["TODOS"]
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("CORE_400"))
            .andExpect(jsonPath("$.violations[0].field").value("transcript"));
    }

    @Test
    @DisplayName("Deve padronizar erros HTTP da integração externa")
    void shouldReturnStandardGatewayError() throws Exception {
        when(service.interpret(any()))
            .thenThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_GATEWAY,
                "O serviço de IA retornou uma resposta inválida."
            ));

        mockMvc.perform(post("/v1/property-search/interpret")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "transcript": "apartamento",
                      "cities": [],
                      "regions": [],
                      "propertyTypes": ["TODOS"]
                    }
                    """))
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.code").value("HTTP_502"))
            .andExpect(jsonPath("$.message").value("O serviço de IA retornou uma resposta inválida."));
    }
}
