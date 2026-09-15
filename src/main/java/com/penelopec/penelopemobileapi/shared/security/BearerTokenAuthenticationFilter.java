package com.penelopec.penelopemobileapi.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(BearerTokenAuthenticationFilter.class);

  private final Optional<TokenValidator> tokenValidator;

  public BearerTokenAuthenticationFilter(Optional<TokenValidator> tokenValidator) {
    this.tokenValidator = tokenValidator;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain) throws ServletException, IOException {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      extractBearerToken(request).ifPresent(token -> authenticate(request, token));
    }

    filterChain.doFilter(request, response);
  }

  private void authenticate(HttpServletRequest request, String token) {
    tokenValidator.flatMap(validator -> validator.validate(token))
      .ifPresentOrElse(
        this::setAuthentication,
        () -> LOGGER.debug("Token inválido para requisição {} {}", request.getMethod(), request.getRequestURI())
      );
  }

  private void setAuthentication(AuthenticatedUser user) {
    List<SimpleGrantedAuthority> authorities = user.roles().stream()
      .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
      .toList();

    var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private Optional<String> extractBearerToken(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      return Optional.empty();
    }

    String token = authorization.substring("Bearer ".length()).trim();
    return token.isBlank() ? Optional.empty() : Optional.of(token);
  }
}
