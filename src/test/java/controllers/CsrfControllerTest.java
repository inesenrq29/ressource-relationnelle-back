package controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.csrf.CsrfToken;

import com.ienrique.ressourceRelationnelle.controller.CsrfController;

class CsrfControllerTest {

  private final CsrfController csrfController = new CsrfController();

  @Test
  @DisplayName("should return CSRF token")
  void shouldReturnCsrfToken() {
    final CsrfToken csrfToken = mock(CsrfToken.class);
    when(csrfToken.getToken()).thenReturn("test-csrf-token");

    final Map<String, String> response = csrfController.csrf(csrfToken);

    assertEquals(Map.of("token", "test-csrf-token"), response);
  }
}
