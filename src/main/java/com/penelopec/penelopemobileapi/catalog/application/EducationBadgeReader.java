package com.penelopec.penelopemobileapi.catalog.application;

import java.util.Optional;

public interface EducationBadgeReader {
  Optional<EducationBadgeResponse> findByEstateId(Long estateId);
}
