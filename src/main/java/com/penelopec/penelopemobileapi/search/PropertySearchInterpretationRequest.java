package com.penelopec.penelopemobileapi.search;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PropertySearchInterpretationRequest(
    @NotBlank(message = "Informe o texto reconhecido para pesquisar.")
    @Size(max = 500, message = "O texto da pesquisa excede o limite permitido.")
    String transcript,
    @NotNull(message = "A lista de cidades é obrigatória.")
    @Size(max = 100, message = "A lista de cidades excede o limite permitido.")
    List<@NotBlank @Size(max = 100) String> cities,
    @NotNull(message = "A lista de regiões é obrigatória.")
    @Size(max = 100, message = "A lista de regiões excede o limite permitido.")
    List<@NotBlank @Size(max = 100) String> regions,
    @NotNull(message = "A lista de tipos de anúncio é obrigatória.")
    @Size(max = 100, message = "A lista de tipos de anúncio excede o limite permitido.")
    List<@NotBlank @Size(max = 100) String> propertyTypes
) {
}
