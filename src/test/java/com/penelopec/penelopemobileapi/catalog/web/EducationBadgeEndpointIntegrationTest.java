package com.penelopec.penelopemobileapi.catalog.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
  "app.badges.enabled=true",
  "app.badges.datasource.url=jdbc:h2:mem:badges_endpoint;MODE=MySQL;DB_CLOSE_DELAY=-1",
  "app.badges.datasource.username=sa",
  "app.badges.datasource.password="
})
@AutoConfigureMockMvc
class EducationBadgeEndpointIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private DataSource dataSource;
  @MockitoBean private JavaMailSender mailSender;

  @BeforeEach
  void setUp() {
    JdbcTemplate app = new JdbcTemplate(dataSource);
    JdbcTemplate badges = new JdbcTemplate(new DriverManagerDataSource(
      "jdbc:h2:mem:badges_endpoint;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
    badges.execute("DROP TABLE IF EXISTS property_badge_score");
    badges.execute("""
      CREATE TABLE property_badge_score (
        property_id BIGINT, badge_type VARCHAR(30), score DECIMAL(5,2),
        granted BOOLEAN, calculation_status VARCHAR(40), radius_meters INT,
        rule_version VARCHAR(50), source_run_id BIGINT, evidence VARCHAR(4000),
        calculated_at TIMESTAMP
      )
      """);
    badges.update("""
      INSERT INTO property_badge_score VALUES
      (900000000101, 'EDUCATION', 71.00, TRUE, 'CALCULATED', 3000, 'education-v1', 1,
       '{"schools_within_radius":5,"nearest_meters":450.0,"distance_method":"STRAIGHT_LINE_HAVERSINE",'
       || '"school_source":"INEP_SCHOOLS","school_source_loaded_at":"2026-10-02T09:00:00"}',
       TIMESTAMP '2026-10-02 10:00:00')
      """);
    app.update("""
      INSERT INTO endereco (id, cidade, uf, municipio_ibge, latitude, longitude,
                            coordenada_origem, coordenada_precisao)
      VALUES (900000000101, 'São Paulo', 'SP', '3550308', -23.55, -46.63, 'GEOCODED', 'ADDRESS')
      """);
    app.update("""
      INSERT INTO empreendimento (id, titulo, descricao, area, quantidade_quartos,
                                  tipo, endereco_id)
      VALUES (900000000101, 'Unidade de teste', 'Descrição de teste', 80, 2,
              'DISPONIVEL', 900000000101)
      """);
    app.update("""
      INSERT INTO anuncio (id, empreendimento_id, preco, ativo, destaque, criado_em)
      VALUES (900000000102, 900000000101, 450000, TRUE, FALSE, CURRENT_TIMESTAMP)
      """);
  }

  @Test
  void detailJoinsAdvertisementToEstateBadgeAcrossTwoDatabases() throws Exception {
    mockMvc.perform(get("/api/v1/advertisements/900000000102").contextPath("/api"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(900000000102L))
      .andExpect(jsonPath("$.estate.id").value(900000000101L))
      .andExpect(jsonPath("$.estate.educationBadge.status").value("CALCULATED"))
      .andExpect(jsonPath("$.estate.educationBadge.score").value(71.0))
      .andExpect(jsonPath("$.estate.educationBadge.schoolsWithinRadius").value(5));
  }
}
