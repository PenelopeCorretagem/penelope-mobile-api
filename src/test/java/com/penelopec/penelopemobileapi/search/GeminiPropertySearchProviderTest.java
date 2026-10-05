package com.penelopec.penelopemobileapi.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@DisplayName("Provedor Gemini para pesquisa de imóveis")
class GeminiPropertySearchProviderTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("Deve enviar a solicitação sem armazená-la e ler os filtros estruturados")
    void shouldSendStatelessRequestAndParseFilters() throws IOException {
        AtomicReference<String> requestBody = new AtomicReference<>();
        AtomicReference<String> receivedApiKey = new AtomicReference<>();
        HttpServer mockGemini = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        mockGemini.createContext("/v1beta/interactions", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes()));
            receivedApiKey.set(exchange.getRequestHeaders().getFirst("x-goog-api-key"));
            byte[] response = """
                {"interaction":{"output_text":"{\\"searchTerm\\":\\"apartamento\\",\\"minBedrooms\\":2,\\"maxPrice\\":500000}"}}
                """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        mockGemini.start();

        try {
            GeminiPropertySearchProvider provider = new GeminiPropertySearchProvider(
                RestClient.builder()
                    .baseUrl("http://127.0.0.1:" + mockGemini.getAddress().getPort() + "/v1beta/interactions")
                    .build(),
                OBJECT_MAPPER,
                new GeminiProperties("test-api-key", "gemini-3.6-flash")
            );
            PropertySearchFilters result = provider.interpret(new PropertySearchInterpretationRequest(
                "Apartamento com dois quartos até 500 mil",
                List.of("São Paulo"),
                List.of("Centro"),
                List.of("TODOS", "LANCAMENTO", "DISPONIVEL", "EM_OBRAS")
            ));
            JsonNode sentRequest = OBJECT_MAPPER.readTree(requestBody.get());

            assertThat(result.searchTerm()).isEqualTo("apartamento");
            assertThat(result.minBedrooms()).isEqualTo(2);
            assertThat(result.maxPrice()).isEqualByComparingTo(new BigDecimal("500000"));
            assertThat(receivedApiKey.get()).isEqualTo("test-api-key");
            assertThat(sentRequest.path("store").asBoolean()).isFalse();
            assertThat(sentRequest.path("model").asText()).isEqualTo("gemini-3.6-flash");
            assertThat(sentRequest.path("response_format").path("mime_type").asText())
                .isEqualTo("application/json");
        } finally {
            mockGemini.stop(0);
        }
    }

    @Test
    @DisplayName("Deve retornar indisponibilidade quando a chave não está configurada")
    void shouldRejectMissingApiKey() {
        GeminiPropertySearchProvider provider = new GeminiPropertySearchProvider(
            RestClient.builder().build(),
            OBJECT_MAPPER,
            new GeminiProperties(" ", "gemini-3.6-flash")
        );

        assertThatThrownBy(() -> provider.interpret(new PropertySearchInterpretationRequest(
            "apartamento",
            List.of(),
            List.of(),
            List.of("TODOS")
        )))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
            .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
