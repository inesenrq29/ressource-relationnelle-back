package controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ienrique.ressourceRelationnelle.RessourceRelationnelleApplication;
import com.ienrique.ressourceRelationnelle.controller.StatController;
import com.ienrique.ressourceRelationnelle.dto.*;
import com.ienrique.ressourceRelationnelle.service.StatService;

@WebMvcTest(StatController.class)
@ContextConfiguration(classes = RessourceRelationnelleApplication.class)
public class StatControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private StatService statService;

  @Nested
  @DisplayName("get stats")
  class GetStats {

    @Test
    @DisplayName("Should get stats")
    void shouldGetStats() throws Exception {
      final StatsDashboardDto dashboard = new StatsDashboardDto();

      when(statService.getStats()).thenReturn(dashboard);

      mockMvc.perform(get("/api/stats").with(jwt()).with(jwt())).andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("get filtered stats")
  class GetFilteredStats {

    @Test
    @DisplayName("Should get filtered stats")
    void shouldGetFilteredStats() throws Exception {
      final StatsDashboardDto dashboard = new StatsDashboardDto();
      final StatsFilterDto filter = new StatsFilterDto();

      when(statService.getStats(filter)).thenReturn(dashboard);

      mockMvc
          .perform(
              post("/api/stats/filter")
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(filter)))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("export stats")
  class ExportStats {

    @Test
    @DisplayName("should export stats")
    void shouldExportStats() throws Exception {
      final StatsFilterDto filter = new StatsFilterDto();
      byte[] csvFile = "col1,col2\nvalue1,value2".getBytes();

      when(statService.exportStats(any(StatsFilterDto.class))).thenReturn(csvFile);

      mockMvc
          .perform(
              post("/api/stats/export")
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(jwt())
                  .content(objectMapper.writeValueAsString(filter)))
          .andExpect(status().isOk())
          .andExpect(header().string("Content-Disposition", "attachment; filename=stats.csv"))
          .andExpect(header().string("Content-Type", "text/csv"))
          .andExpect(content().bytes(csvFile));
    }
  }
}
