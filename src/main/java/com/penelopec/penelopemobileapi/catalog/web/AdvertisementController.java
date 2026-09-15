package com.penelopec.penelopemobileapi.catalog.web;

import com.penelopec.penelopemobileapi.catalog.application.AdvertisementResponse;
import com.penelopec.penelopemobileapi.catalog.application.CatalogService;
import com.penelopec.penelopemobileapi.catalog.infrastructure.persistence.EstateType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {
  private final CatalogService catalogService;

  @GetMapping
  public ResponseEntity<List<AdvertisementResponse>> findAll(
    @RequestParam(name = "type", required = false) EstateType type,
    @RequestParam(name = "active", defaultValue = "true") boolean active) {
    return ResponseEntity.ok(catalogService.findAll(type, active));
  }

  @GetMapping("/{id}")
  public ResponseEntity<AdvertisementResponse> findById(@PathVariable("id") Long id) {
    return ResponseEntity.ok(catalogService.findActiveById(id));
  }
}