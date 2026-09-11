package com.penelopec.penelopemobileapi.catalog.web;

import com.penelopec.penelopemobileapi.catalog.application.AdvertisementResponse;
import com.penelopec.penelopemobileapi.catalog.application.CatalogService;
import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.EstateType;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdvertisementControllerTest {

  private final CatalogService catalogService = mock(CatalogService.class);
  private final MockMvc mockMvc = MockMvcBuilders
    .standaloneSetup(new AdvertisementController(catalogService))
    .build();

  @Test
  void shouldListActiveAdvertisementsFilteredByEstateType() throws Exception {
    when(catalogService.findAll(EstateType.LANCAMENTO, true)).thenReturn(List.of(advertisement()));

    mockMvc.perform(get("/v1/advertisements")
        .param("type", "LANCAMENTO")
        .param("active", "true"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].estate.type").value("LANCAMENTO"));

    verify(catalogService).findAll(EstateType.LANCAMENTO, true);
  }

  @Test
  void shouldReturnActiveAdvertisementDetails() throws Exception {
    when(catalogService.findActiveById(1L)).thenReturn(advertisement());

    mockMvc.perform(get("/v1/advertisements/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.price").value(450000))
      .andExpect(jsonPath("$.estate.images[0].type").value("Imagem"))
      .andExpect(jsonPath("$.estate.amenities[0].description").value("Piscina"));

    verify(catalogService).findActiveById(1L);
  }

  private AdvertisementResponse advertisement() {
    return new AdvertisementResponse(
      1L,
      BigDecimal.valueOf(450_000),
      true,
      false,
      LocalDateTime.of(2026, 9, 10, 10, 30),
      new AdvertisementResponse.EstateResponse(
        "Residencial Aurora",
        "Apartamento com vista livre.",
        82.0,
        3,
        "LANCAMENTO",
        new AdvertisementResponse.AddressResponse("São Paulo", "Centro", "SP", -23.5505, -46.6333),
        Set.of(new AdvertisementResponse.MediaResponse(1L, "https://images.example.com/aurora.jpg", "Imagem")),
        Set.of(new AdvertisementResponse.AmenityResponse(1L, "Piscina", "pool"))
      )
    );
  }
}