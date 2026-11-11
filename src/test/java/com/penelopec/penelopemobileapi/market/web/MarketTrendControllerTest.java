package com.penelopec.penelopemobileapi.market.web;

import com.penelopec.penelopemobileapi.market.infrastructure.JdbcMarketTrendReader;
import com.penelopec.penelopemobileapi.shared.web.error.GlobalExceptionHandler;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MarketTrendControllerTest {
  private static final String URL = "/v1/market-trends/neighborhoods";

  private final StaticListableBeanFactory beans = new StaticListableBeanFactory();
  private JdbcMarketTrendReader reader;
  private JdbcTemplate jdbc;
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    var config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
    config.setUsername("sa");
    var source = new HikariDataSource(config);
    reader = new JdbcMarketTrendReader(source);
    jdbc = new JdbcTemplate(source);
    jdbc.execute("""
      CREATE TABLE itbi_trend_run (
        id BIGINT PRIMARY KEY, rule_version VARCHAR(50), start_year INT, through_year INT, payment_cutoff CHAR(7)
      )
      """);
    jdbc.execute("""
      CREATE TABLE itbi_neighborhood_year (
        trend_run_id BIGINT, neighborhood_key VARCHAR(120), neighborhood VARCHAR(120),
        transaction_year INT, sample_count INT, median_price_sqm DECIMAL(24,2), comparison_year INT,
        nominal_change_pct DECIMAL(24,2), status VARCHAR(30), comparison_status VARCHAR(30)
      )
      """);
    beans.addBean("reader", reader);
    mvc = mvc(beans);
  }

  @AfterEach
  void tearDown() {
    reader.close();
  }

  @Test
  void returnsTheHistoricalRunAndPreservesNullsAndYearOrder() throws Exception {
    jdbc.update("""
      INSERT INTO itbi_trend_run VALUES
        (1, 'itbi-apartments-v1', 2010, 2025, '2026-09'),
        (2, 'itbi-apartments-v1', 2024, 2025, '2026-09'),
        (3, 'itbi-experimental', 2010, 2026, '2026-09'),
        (4, 'itbi-apartments-v1', 2010, 2025, '2025-12')
      """);
    jdbc.update("""
      INSERT INTO itbi_neighborhood_year VALUES
        (1, 'TATUAPE', 'Tatuapé', 2025, 413, 4205.47, 2024, 3.91, 'DISPONIVEL', 'DISPONIVEL'),
        (1, 'TATUAPE', 'Tatuapé', 2010, 1, NULL, NULL, NULL, 'DADOS_INSUFICIENTES', 'SEM_ANO_BASE'),
        (1, 'MOEMA', 'Moema', 2025, 178, 6634.08, 2024, 0.22, 'DISPONIVEL', 'DISPONIVEL'),
        (2, 'TATUAPE', 'Tatuapé', 2025, 486, 4220.86, 2024, 3.63, 'DISPONIVEL', 'DISPONIVEL'),
        (3, 'TATUAPE', 'Tatuapé', 2025, 486, 4220.86, 2024, 3.63, 'DISPONIVEL', 'DISPONIVEL'),
        (4, 'TATUAPE', 'Tatuapé', 2025, 486, 4220.86, 2024, 3.63, 'DISPONIVEL', 'DISPONIVEL')
      """);

    mvc.perform(get(URL))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.snapshot.id").value(1))
      .andExpect(jsonPath("$.snapshot.paymentCutoff").value("2026-09"))
      .andExpect(jsonPath("$.neighborhoods[0].key").value("MOEMA"))
      .andExpect(jsonPath("$.neighborhoods[1].name").value("Tatuapé"));

    mvc.perform(get(URL + "/{key}", "tatuapé"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.neighborhood.key").value("TATUAPE"))
      .andExpect(jsonPath("$.years[0].year").value(2010))
      .andExpect(jsonPath("$.years[0].medianPriceSqm").value(nullValue()))
      .andExpect(jsonPath("$.years[0].nominalChangePct").value(nullValue()))
      .andExpect(jsonPath("$.years[0].status").value("DADOS_INSUFICIENTES"))
      .andExpect(jsonPath("$.years[1].comparisonYear").value(2024))
      .andExpect(jsonPath("$.years[1].medianPriceSqm").value(4205.47))
      .andExpect(jsonPath("$.years[1].nominalChangePct").value(3.91));

    mvc.perform(get(URL + "/LIBERDADE"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("TREND_NEIGHBORHOOD_NOT_FOUND"));
  }

  @Test
  void distinguishesAnUnavailableSeriesFromAnInvalidKey() throws Exception {
    mvc.perform(get(URL))
      .andExpect(status().isServiceUnavailable())
      .andExpect(jsonPath("$.code").value("MARKET_TRENDS_UNAVAILABLE"));

    mvc(new StaticListableBeanFactory()).perform(get(URL))
      .andExpect(status().isServiceUnavailable());

    mvc.perform(get(URL + "/---"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("INVALID_TREND_NEIGHBORHOOD"));

    jdbc.execute("DROP TABLE itbi_trend_run");
    mvc.perform(get(URL))
      .andExpect(status().isServiceUnavailable())
      .andExpect(jsonPath("$.code").value("MARKET_TRENDS_UNAVAILABLE"));
  }

  private static MockMvc mvc(StaticListableBeanFactory beans) {
    return MockMvcBuilders
      .standaloneSetup(new MarketTrendController(beans.getBeanProvider(JdbcMarketTrendReader.class)))
      .setControllerAdvice(new GlobalExceptionHandler())
      .build();
  }
}
