package com.penelopec.penelopemobileapi.search;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/property-search")
@RequiredArgsConstructor
public class PropertySearchController {
    private final PropertySearchInterpretationService interpretationService;

    @PostMapping("/interpret")
    public ResponseEntity<PropertySearchFilters> interpret(
        @Valid @RequestBody PropertySearchInterpretationRequest request
    ) {
        return ResponseEntity.ok(interpretationService.interpret(request));
    }
}
