package com.penelopec.penelopemobileapi.favorite.web;

import com.penelopec.penelopemobileapi.favorite.application.FavoriteResponse;
import com.penelopec.penelopemobileapi.favorite.application.FavoriteService;
import com.penelopec.penelopemobileapi.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/users/me/favorites")
@RequiredArgsConstructor
public class FavoriteController {
  private final FavoriteService favoriteService;

  @GetMapping
  public ResponseEntity<List<FavoriteResponse>> findAll(@AuthenticationPrincipal AuthenticatedUser user) {
    return ResponseEntity.ok(favoriteService.findAll(user.email()));
  }

  @PutMapping("/{advertisementId}")
  public ResponseEntity<Void> add(
    @AuthenticationPrincipal AuthenticatedUser user,
    @PathVariable("advertisementId") Long advertisementId) {
    favoriteService.add(user.email(), advertisementId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{advertisementId}")
  public ResponseEntity<Void> remove(
    @AuthenticationPrincipal AuthenticatedUser user,
    @PathVariable("advertisementId") Long advertisementId) {
    favoriteService.remove(user.email(), advertisementId);
    return ResponseEntity.noContent().build();
  }
}