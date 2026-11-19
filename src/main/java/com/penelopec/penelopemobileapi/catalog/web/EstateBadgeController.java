package com.penelopec.penelopemobileapi.catalog.web;

import com.penelopec.penelopemobileapi.catalog.application.EstateBadgeService;
import com.penelopec.penelopemobileapi.catalog.application.EstateBadgesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/estates")
@RequiredArgsConstructor
public class EstateBadgeController {
  private final EstateBadgeService badgeService;

  @GetMapping("/{estateId}/badges")
  public ResponseEntity<EstateBadgesResponse> findBadges(@PathVariable("estateId") Long estateId) {
    return ResponseEntity.ok(badgeService.findByEstateId(estateId));
  }
}
