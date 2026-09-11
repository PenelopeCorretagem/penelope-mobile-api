package com.penelopec.penelopemobileapi.shared.web.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

  static final String REQUEST_ID_HEADER = "X-Request-Id";
  private static final String REQUEST_ID_MDC_KEY = "requestId";

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain) throws ServletException, IOException {
    String requestId = resolveRequestId(request.getHeader(REQUEST_ID_HEADER));
    long startedAtNanos = System.nanoTime();

    MDC.put(REQUEST_ID_MDC_KEY, requestId);
    response.setHeader(REQUEST_ID_HEADER, requestId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      long durationMillis = (System.nanoTime() - startedAtNanos) / 1_000_000;
      log.info("http_request method={} path={} status={} duration_ms={}",
        request.getMethod(), request.getRequestURI(), response.getStatus(), durationMillis);
      MDC.remove(REQUEST_ID_MDC_KEY);
    }
  }

  private String resolveRequestId(String suppliedRequestId) {
    if (suppliedRequestId == null || suppliedRequestId.isBlank()) {
      return UUID.randomUUID().toString();
    }

    try {
      return UUID.fromString(suppliedRequestId).toString();
    } catch (IllegalArgumentException exception) {
      return UUID.randomUUID().toString();
    }
  }
}
