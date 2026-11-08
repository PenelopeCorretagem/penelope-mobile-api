package com.penelopec.penelopemobileapi.catalog.application;

import java.util.List;

public record EstateBadgesResponse(Long estateId, int scaleMax, List<BadgeResponse> badges) {
}
