package com.penelopec.penelopemobileapi.market.infrastructure;

import com.penelopec.penelopemobileapi.market.application.MarketTrendResponse;
import com.penelopec.penelopemobileapi.market.application.MarketTrendResponse.Neighborhood;
import com.penelopec.penelopemobileapi.market.application.MarketTrendResponse.Snapshot;
import com.penelopec.penelopemobileapi.market.application.MarketTrendResponse.Year;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMarketTrendReader implements AutoCloseable {
  private static final String RULE_VERSION = "itbi-apartments-v1";

  private final HikariDataSource source;
  private final JdbcTemplate jdbc;

  public JdbcMarketTrendReader(HikariDataSource source) {
    this.source = source;
    this.jdbc = new JdbcTemplate(source);
    this.jdbc.setQueryTimeout(3);
  }

  public Optional<Snapshot> findSnapshot() {
    return jdbc.query("""
      SELECT r.id, r.rule_version, r.start_year, r.through_year, r.payment_cutoff
      FROM itbi_trend_run r
      WHERE r.rule_version = ?
        AND EXISTS (
          SELECT 1 FROM itbi_neighborhood_year n
          WHERE n.trend_run_id = r.id AND n.nominal_change_pct IS NOT NULL
        )
      ORDER BY r.start_year ASC, r.through_year DESC, r.payment_cutoff DESC, r.id DESC
      LIMIT 1
      """, (row, index) -> new Snapshot(
      row.getLong("id"), row.getString("rule_version"), row.getInt("start_year"),
      row.getInt("through_year"), row.getString("payment_cutoff")
    ), RULE_VERSION).stream().findFirst();
  }

  public List<Neighborhood> findNeighborhoods(Snapshot snapshot) {
    return jdbc.query("""
      SELECT DISTINCT neighborhood_key, neighborhood
      FROM itbi_neighborhood_year
      WHERE trend_run_id = ?
      ORDER BY neighborhood_key
      """, (row, index) -> new Neighborhood(row.getString("neighborhood_key"), row.getString("neighborhood")),
      snapshot.id());
  }

  public Optional<MarketTrendResponse> findSeries(Snapshot snapshot, String key) {
    return jdbc.query("""
      SELECT neighborhood_key, neighborhood, transaction_year, sample_count, median_price_sqm,
             comparison_year, nominal_change_pct, status, comparison_status
      FROM itbi_neighborhood_year
      WHERE trend_run_id = ? AND neighborhood_key = ?
      ORDER BY transaction_year
      """, rows -> {
      if (!rows.next()) {
        return Optional.empty();
      }
      var neighborhood = new Neighborhood(rows.getString("neighborhood_key"), rows.getString("neighborhood"));
      var years = new ArrayList<Year>();
      do {
        years.add(new Year(
          rows.getInt("transaction_year"), rows.getInt("sample_count"), rows.getBigDecimal("median_price_sqm"),
          rows.getObject("comparison_year", Integer.class), rows.getBigDecimal("nominal_change_pct"),
          rows.getString("status"), rows.getString("comparison_status")
        ));
      } while (rows.next());
      return Optional.of(new MarketTrendResponse(snapshot, neighborhood, List.copyOf(years)));
    }, snapshot.id(), key);
  }

  @Override
  public void close() {
    source.close();
  }
}
