package com.penelopec.penelopemobileapi.search;

import com.penelopec.penelopemobileapi.shared.core.exception.DomainError;
import com.penelopec.penelopemobileapi.shared.core.exception.ValidationException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PropertySearchInterpretationService {
    private static final int MAX_TRANSCRIPT_LENGTH = 500;
    private static final int MAX_CATALOG_SIZE = 100;
    private static final int MAX_CATALOG_VALUE_LENGTH = 100;
    private static final Set<String> ALLOWED_TYPES = Set.of(
        "TODOS",
        "LANCAMENTO",
        "DISPONIVEL",
        "EM_OBRAS"
    );
    private static final Set<String> ALLOWED_SORT_ORDERS = Set.of(
        "distance",
        "asc",
        "desc"
    );

    private final PropertySearchInterpretationProvider provider;

    public PropertySearchFilters interpret(PropertySearchInterpretationRequest request) {
        validateRequest(request);
        PropertySearchFilters interpreted = provider.interpret(request);
        return validateFilters(interpreted, request);
    }

    private static void validateRequest(PropertySearchInterpretationRequest request) {
        if (request == null || request.transcript() == null || request.transcript().isBlank()) {
            throw invalidRequest("Informe o texto reconhecido para pesquisar.");
        }
        if (request.transcript().length() > MAX_TRANSCRIPT_LENGTH) {
            throw invalidRequest("O texto da pesquisa excede o limite permitido.");
        }

        validateCatalog(request.cities(), "cidades");
        validateCatalog(request.regions(), "regiões");
        validateCatalog(request.propertyTypes(), "tipos de anúncio");
        if (request.propertyTypes().stream().anyMatch(type -> !ALLOWED_TYPES.contains(type))) {
            throw invalidRequest("A lista de tipos de anúncio contém um valor inválido.");
        }
    }

    private static void validateCatalog(List<String> values, String label) {
        if (values == null || values.size() > MAX_CATALOG_SIZE) {
            throw invalidRequest("A lista de " + label + " é inválida.");
        }
        if (values.stream().anyMatch(value ->
            value == null || value.isBlank() || value.length() > MAX_CATALOG_VALUE_LENGTH
        )) {
            throw invalidRequest("A lista de " + label + " contém um valor inválido.");
        }
    }

    private static PropertySearchFilters validateFilters(
        PropertySearchFilters filters,
        PropertySearchInterpretationRequest request
    ) {
        if (filters == null) {
            throw invalidProviderResponse("A IA retornou filtros inválidos.");
        }

        String searchTerm = filters.searchTerm() == null ? "" : filters.searchTerm().trim();
        if (searchTerm.length() > MAX_CATALOG_VALUE_LENGTH) {
            throw invalidProviderResponse("A IA retornou um termo de pesquisa inválido.");
        }

        validateCatalogValue(filters.city(), request.cities(), "cidade");
        validateCatalogValue(filters.region(), request.regions(), "região");
        validateCatalogValue(filters.type(), request.propertyTypes(), "tipo de anúncio");

        String type = filters.type() == null ? "TODOS" : filters.type();
        if (!ALLOWED_TYPES.contains(type)) {
            throw invalidProviderResponse("A IA retornou um tipo de anúncio inválido.");
        }

        String sortOrder = filters.sortOrder() == null ? "distance" : filters.sortOrder();
        if (!ALLOWED_SORT_ORDERS.contains(sortOrder)) {
            throw invalidProviderResponse("A IA retornou uma ordenação inválida.");
        }

        Integer minBedrooms = filters.minBedrooms();
        if (minBedrooms != null && (minBedrooms < 1 || minBedrooms > 20)) {
            throw invalidProviderResponse("A IA retornou uma quantidade de dormitórios inválida.");
        }

        BigDecimal maxPrice = filters.maxPrice();
        if (maxPrice != null && (
            maxPrice.signum() <= 0 || maxPrice.compareTo(new BigDecimal("1000000000")) > 0
        )) {
            throw invalidProviderResponse("A IA retornou um preço máximo inválido.");
        }

        return new PropertySearchFilters(
            searchTerm,
            filters.city(),
            filters.region(),
            type,
            minBedrooms,
            maxPrice,
            sortOrder
        );
    }

    private static void validateCatalogValue(String value, List<String> allowedValues, String label) {
        if (value != null && !allowedValues.contains(value)) {
            throw invalidProviderResponse("A IA retornou uma " + label + " fora das opções disponíveis.");
        }
    }

    private static ValidationException invalidRequest(String message) {
        return new ValidationException(DomainError.of("PROPERTY_SEARCH_INVALID", message));
    }

    private static ResponseStatusException invalidProviderResponse(String message) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
    }
}
