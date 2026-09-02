package com.ienrique.ressourceRelationnelle.utils;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimiter implements Filter {

  private static final String LOGIN_ENDPOINT = "/api/auth/login";
  private static final String REGISTER_ENDPOINT = "/api/auth/register";

  private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

  private Bucket createNewBucket() {
    return Bucket.builder()
        .addLimit(limit -> limit.capacity(5).refillIntervally(5, Duration.ofMinutes(1)))
        .build();
  }

  @Override
  public void doFilter(
      final ServletRequest servletRequest,
      final ServletResponse servletResponse,
      final FilterChain filterChain)
      throws IOException, ServletException {

    final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
    final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

    final String requestUri = httpRequest.getRequestURI();

    final boolean isPostRequest = "POST".equalsIgnoreCase(httpRequest.getMethod());

    final boolean isSensitiveEndpoint =
        LOGIN_ENDPOINT.equals(requestUri) || REGISTER_ENDPOINT.equals(requestUri);

    if (isPostRequest && isSensitiveEndpoint) {
      final String ipAddress = httpRequest.getRemoteAddr();

      final String clientKey = ipAddress + "|" + requestUri;

      final Bucket bucket = cache.computeIfAbsent(clientKey, key -> createNewBucket());

      if (!bucket.tryConsume(1)) {
        httpResponse.setStatus(429);
        httpResponse.setContentType("text/plain");
        httpResponse.setHeader("Retry-After", "60");
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.getWriter().write("Trop de tentatives, veuillez réessayer plus tard.");
        return;
      }
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }
}
