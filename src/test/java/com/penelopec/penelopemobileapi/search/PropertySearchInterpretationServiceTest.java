package com.penelopec.penelopemobileapi.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.penelopec.penelopemobileapi.shared.core.exception.ValidationException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Interpretação da pesquisa de imóveis")
class PropertySearchInterpretationServiceTest {
    private static final PropertySearchInterpretationRequest VALID_REQUEST =
        new PropertySearchInterpretationRequest(
            "Apartamento com pelo menos dois quartos até 500 mil",
            List.of("São Paulo"),
            List.of("Centro"),
            List.of("TODOS", "LANCAMENTO", "DISPONIVEL", "EM_OBRAS")
        );

    private PropertySearchInterpretationProvider provider;
    private PropertySearchInterpretationService service;

    @BeforeEach
    void setUp() {
        provider = mock(PropertySearchInterpretationProvider.class);
        service = new PropertySearchInterpretationService(provider);
    }

    @Test
    @DisplayName("Deve validar e preservar os filtros retornados pelo provedor")
    void shouldReturnValidatedFilters() {
        PropertySearchFilters expected = new PropertySearchFilters(
            "apartamento",
            "São Paulo",
            null,
            "TODOS",
            2,
            new BigDecimal("500000"),
            "distance"
        );
        when(provider.interpret(VALID_REQUEST)).thenReturn(expected);

        PropertySearchFilters result = service.interpret(VALID_REQUEST);

        assertThat(result).isEqualTo(expected);
        verify(provider).interpret(VALID_REQUEST);
    }

    @Test
    @DisplayName("Deve rejeitar texto vazio antes de chamar o provedor")
    void shouldRejectBlankTranscript() {
        PropertySearchInterpretationRequest request = new PropertySearchInterpretationRequest(
            " ",
            List.of(),
            List.of(),
            List.of("TODOS")
        );

        assertThatThrownBy(() -> service.interpret(request))
            .isInstanceOf(ValidationException.class)
            .hasMessage("Informe o texto reconhecido para pesquisar.");
        verify(provider, never()).interpret(request);
    }

    @Test
    @DisplayName("Deve rejeitar valores de localidade fora do catálogo")
    void shouldRejectLocationOutsideCatalog() {
        when(provider.interpret(VALID_REQUEST)).thenReturn(new PropertySearchFilters(
            "",
            "Cidade inventada",
            null,
            "TODOS",
            null,
            null,
            "distance"
        ));

        assertThatThrownBy(() -> service.interpret(VALID_REQUEST))
            .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
            .hasMessageContaining("A IA retornou uma cidade fora das opções disponíveis.");
    }

    @Test
    @DisplayName("Deve rejeitar catálogo com tipo de anúncio desconhecido")
    void shouldRejectUnsupportedPropertyType() {
        PropertySearchInterpretationRequest request = new PropertySearchInterpretationRequest(
            "apartamento",
            List.of(),
            List.of(),
            List.of("CASA")
        );

        assertThatThrownBy(() -> service.interpret(request))
            .isInstanceOf(ValidationException.class)
            .hasMessage("A lista de tipos de anúncio contém um valor inválido.");
        verify(provider, never()).interpret(request);
    }

    @Test
    @DisplayName("Deve rejeitar preço máximo inválido")
    void shouldRejectInvalidMaximumPrice() {
        when(provider.interpret(VALID_REQUEST)).thenReturn(new PropertySearchFilters(
            "",
            null,
            null,
            "TODOS",
            null,
            BigDecimal.ZERO,
            "distance"
        ));

        assertThatThrownBy(() -> service.interpret(VALID_REQUEST))
            .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
            .hasMessageContaining("A IA retornou um preço máximo inválido.");
    }
}
