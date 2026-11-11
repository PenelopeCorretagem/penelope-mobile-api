package com.penelopec.penelopemobileapi.market.web;

import com.penelopec.penelopemobileapi.market.application.MarketTrendResponse;
import com.penelopec.penelopemobileapi.market.application.MarketTrendResponse.Neighborhoods;
import com.penelopec.penelopemobileapi.market.application.MarketTrendResponse.Snapshot;
import com.penelopec.penelopemobileapi.market.infrastructure.JdbcMarketTrendReader;
import com.penelopec.penelopemobileapi.shared.core.exception.DomainError;
import com.penelopec.penelopemobileapi.shared.core.exception.NotFoundException;
import com.penelopec.penelopemobileapi.shared.core.exception.ValidationException;
import com.penelopec.penelopemobileapi.shared.web.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.Normalizer;
import java.util.Locale;

@RestController
@RequestMapping("/v1/market-trends/neighborhoods")
public class MarketTrendController {
  private static final Logger LOGGER = LoggerFactory.getLogger(MarketTrendController.class);

  private final ObjectProvider<JdbcMarketTrendReader> readers;

  public MarketTrendController(ObjectProvider<JdbcMarketTrendReader> readers) {
    this.readers = readers;
  }

  @GetMapping
  @Operation(summary = "Lista os bairros com série histórica de ITBI")
  public Neighborhoods findNeighborhoods() {
    var reader = reader();
    var snapshot = snapshot(reader);
    return new Neighborhoods(snapshot, reader.findNeighborhoods(snapshot));
  }

  @GetMapping("/{key}")
  @Operation(summary = "Consulta o valor declarado por m² e a variação anual de apartamentos no bairro")
  public MarketTrendResponse findSeries(@PathVariable("key") String key) {
    String normalized = normalizeKey(key);
    var reader = reader();
    return reader.findSeries(snapshot(reader), normalized)
      .orElseThrow(() -> new NotFoundException(
        DomainError.of("TREND_NEIGHBORHOOD_NOT_FOUND", "Bairro sem série de ITBI disponível")));
  }

  @ExceptionHandler({TrendUnavailable.class, DataAccessException.class})
  public ResponseEntity<ApiErrorResponse> unavailable(RuntimeException error, HttpServletRequest request) {
    if (error instanceof DataAccessException) {
      LOGGER.warn("Falha na consulta de tendências do ITBI ({})", error.getClass().getSimpleName());
    }
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiErrorResponse.of(
      503, "MARKET_TRENDS_UNAVAILABLE", "Série de tendências indisponível", request.getRequestURI()
    ));
  }

  private JdbcMarketTrendReader reader() {
    return readers.getIfAvailable(() -> { throw new TrendUnavailable(); });
  }

  private static Snapshot snapshot(JdbcMarketTrendReader reader) {
    return reader.findSnapshot().orElseThrow(TrendUnavailable::new);
  }

  private static String normalizeKey(String key) {
    String normalized = Normalizer.normalize(key, Normalizer.Form.NFKD)
      .replaceAll("\\p{M}+", "").toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", " ").strip();
    if (normalized.isEmpty() || normalized.length() > 120) {
      throw new ValidationException(DomainError.of("INVALID_TREND_NEIGHBORHOOD", "Chave de bairro inválida"));
    }
    return normalized;
  }

  private static class TrendUnavailable extends RuntimeException {}
}
