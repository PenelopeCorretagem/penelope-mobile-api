package com.penelopec.penelopemobileapi.catalog.application;

import java.util.List;

public interface BadgeReader {
  /** Lança DataAccessException quando o banco analítico não responde. */
  List<BadgeRecord> findByEstateId(Long estateId);

  /** Falso quando a leitura está desativada por configuração. */
  default boolean isEnabled() {
    return true;
  }
}
