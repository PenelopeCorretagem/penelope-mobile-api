package com.penelopec.penelopemobileapi.catalog.web;

import com.penelopec.penelopemobileapi.catalog.application.BadgeResponse;
import com.penelopec.penelopemobileapi.catalog.application.EstateBadgeService;
import com.penelopec.penelopemobileapi.catalog.application.EstateBadgesResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EstateBadgeControllerTest {
  private final EstateBadgeService service = mock(EstateBadgeService.class);
  private final MockMvc mockMvc = MockMvcBuilders
    .standaloneSetup(new EstateBadgeController(service))
    .build();

  @Test
  void exposesBadgesContract() throws Exception {
    when(service.findByEstateId(42L)).thenReturn(new EstateBadgesResponse(42L, 10, List.of(
      new BadgeResponse("EDUCATION", "Escolas próximas", new BigDecimal("8.3"), true,
        "CALCULATED", null, "5 escolas em até 3 km.", Map.of("schoolsWithinRadius", 5)),
      new BadgeResponse("SAFETY_OCCURRENCES", "Segurança", null, false,
        "UNAVAILABLE", "NOT_CALCULATED", "Ainda não calculada.", Map.of()))));

    mockMvc.perform(get("/v1/estates/42/badges"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.estateId").value(42))
      .andExpect(jsonPath("$.scaleMax").value(10))
      .andExpect(jsonPath("$.badges[0].type").value("EDUCATION"))
      .andExpect(jsonPath("$.badges[0].score").value(8.3))
      .andExpect(jsonPath("$.badges[0].evidence.schoolsWithinRadius").value(5))
      .andExpect(jsonPath("$.badges[1].status").value("UNAVAILABLE"))
      .andExpect(jsonPath("$.badges[1].score").isEmpty());
  }
}
