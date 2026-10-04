package com.penelopec.penelopemobileapi.catalog.web;

import com.penelopec.penelopemobileapi.catalog.application.AdvertisementResponse;
import com.penelopec.penelopemobileapi.catalog.application.CatalogService;
import com.penelopec.penelopemobileapi.catalog.application.EducationBadgeResponse;
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
    when(catalogService.findAll(EstateType.LANCAMENTO, true)).thenReturn(List.of(advertisement(null)));

    mockMvc.perform(get("/v1/advertisements")
        .param("type", "LANCAMENTO")
        .param("active", "true"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].estate.type").value("LANCAMENTO"))
      .andExpect(jsonPath("$[0].estate.educationBadge").doesNotExist());

    verify(catalogService).findAll(EstateType.LANCAMENTO, true);
  }

  @Test
  void shouldReturnActiveAdvertisementDetails() throws Exception {
    when(catalogService.findActiveById(1L)).thenReturn(advertisement(educationBadge()));

    mockMvc.perform(get("/v1/advertisements/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.price").value(450000))
      .andExpect(jsonPath("$.estate.id").value(10))
      .andExpect(jsonPath("$.estate.address.municipalityIbgeCode").value("3550308"))
      .andExpect(jsonPath("$.estate.educationBadge.status").value("CALCULATED"))
      .andExpect(jsonPath("$.estate.educationBadge.schoolsWithinRadius").value(5))
      .andExpect(jsonPath("$.estate.images[0].type").value("Imagem"))
      .andExpect(jsonPath("$.estate.amenities[0].description").value("Piscina"));

    verify(catalogService).findActiveById(1L);
  }

  private AdvertisementResponse advertisement(EducationBadgeResponse educationBadge) {
    return new AdvertisementResponse(
      1L,
      BigDecimal.valueOf(450_000),
      true,
      false,
      LocalDateTime.of(2026, 9, 10, 10, 30),
      new AdvertisementResponse.EstateResponse(
        10L,
        "Residencial Aurora",
        "Apartamento com vista livre.",
        82.0,
        3,
        "LANCAMENTO",
        new AdvertisementResponse.AddressResponse(
          "São Paulo", "Centro", "SP", "3550308", -23.5505, -46.6333,
          "GEOCODED", "ADDRESS", LocalDateTime.of(2026, 9, 10, 9, 0)
        ),
        Set.of(new AdvertisementResponse.MediaResponse(1L, "https://images.example.com/aurora.jpg", "Imagem")),
        Set.of(new AdvertisementResponse.AmenityResponse(1L, "Piscina", "pool")),
        educationBadge
      )
    );
  }

  private EducationBadgeResponse educationBadge() {
    return new EducationBadgeResponse(
      "CALCULATED", BigDecimal.valueOf(71), true, 3000, 5,
      BigDecimal.valueOf(450), "STRAIGHT_LINE_HAVERSINE", "education-v1",
      "INEP_SCHOOLS", 1L, LocalDateTime.of(2026, 10, 2, 9, 0),
      LocalDateTime.of(2026, 10, 2, 10, 0)
    );
  }
}
