package com.penelopec.penelopemobileapi.favorite.application;

import java.time.Instant;

public record FavoriteResponse(Long advertisementId, Instant createdAt) {
}