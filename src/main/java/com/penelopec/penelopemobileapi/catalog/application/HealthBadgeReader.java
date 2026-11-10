package com.penelopec.penelopemobileapi.catalog.application;

import java.util.List;

public interface HealthBadgeReader {
  List<HealthBadgeResponse> findHealthByEstateId(Long estateId);
}
