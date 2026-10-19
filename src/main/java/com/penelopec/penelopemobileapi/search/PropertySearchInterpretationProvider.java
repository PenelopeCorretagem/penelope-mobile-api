package com.penelopec.penelopemobileapi.search;

public interface PropertySearchInterpretationProvider {
    PropertySearchFilters interpret(PropertySearchInterpretationRequest request);
}
