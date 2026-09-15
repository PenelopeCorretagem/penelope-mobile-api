package com.penelopec.penelopemobileapi.shared.web.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class HttpRequestLoggingFilterTest {

  private final HttpRequestLoggingFilter filter = new HttpRequestLoggingFilter();

  @Test
  void shouldPropagateValidRequestIdAndClearMdcAfterRequest() throws Exception {
    String requestId = UUID.randomUUID().toString();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/auth/login");
    request.addHeader(HttpRequestLoggingFilter.REQUEST_ID_HEADER, requestId);
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<String> requestIdInMdc = new AtomicReference<>();

    filter.doFilter(request, response, (ignoredRequest, ignoredResponse) ->
      requestIdInMdc.set(MDC.get("requestId")));

    assertThat(response.getHeader(HttpRequestLoggingFilter.REQUEST_ID_HEADER)).isEqualTo(requestId);
    assertThat(requestIdInMdc.get()).isEqualTo(requestId);
    assertThat(MDC.get("requestId")).isNull();
  }

  @Test
  void shouldGenerateRequestIdWhenHeaderIsInvalid() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
    request.addHeader(HttpRequestLoggingFilter.REQUEST_ID_HEADER, "invalid-request-id");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> { });

    assertThat(response.getHeader(HttpRequestLoggingFilter.REQUEST_ID_HEADER))
      .matches(value -> UUID.fromString(value) != null);
  }
}
