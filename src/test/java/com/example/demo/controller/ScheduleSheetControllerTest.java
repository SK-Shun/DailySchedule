package com.example.demo.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.ScheduleSheetDto;
import com.example.demo.entity.ScheduleSheet;
import com.example.demo.service.ScheduleQueryService;
import com.example.demo.service.ScheduleSheetService;
import com.example.demo.web.support.ScheduleDetailPageBuilder;

@WebMvcTest(ScheduleSheetController.class)
class ScheduleSheetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleSheetService scheduleSheetService;

    @MockitoBean
    private ScheduleQueryService scheduleQueryService;
    
    @MockitoBean
    private ScheduleDetailPageBuilder pageBuilder;

    @Test
    @DisplayName("GET /schedule-sheets はシート一覧をmodelに入れて一覧画面を表示する")
    void indexReturnsScheduleSheetListView() throws Exception {
        ScheduleSheetDto sheet = new ScheduleSheetDto(
                "My Schedule",
                "my-schedule"
        );

        when(scheduleQueryService.findAllSheets())
                .thenReturn(List.of(sheet));

        mockMvc.perform(get("/schedule-sheets"))
                .andExpect(status().isOk())
                .andExpect(view().name("schedule-sheets"))
                .andExpect(model().attributeExists("sheets"))
                .andExpect(model().attribute("sheets", hasSize(1)))
                .andExpect(model().attribute("sheets", hasItem(
                        hasProperty("title", is("My Schedule"))
                )))
                .andExpect(model().attribute("sheets", hasItem(
                        hasProperty("slug", is("my-schedule"))
                )));

        verify(scheduleQueryService, times(1)).findAllSheets();
    }

    @Test
    @DisplayName("POST /schedule-sheets はシートを作成して詳細画面へリダイレクトする")
    void createRedirectsToCreatedScheduleSheetDetailPage() throws Exception {
        ScheduleSheet createdSheet = mock(ScheduleSheet.class);
        when(createdSheet.getSlug()).thenReturn("my-schedule");

        when(scheduleSheetService.createSheet("My Schedule"))
                .thenReturn(createdSheet);

        mockMvc.perform(post("/schedule-sheets")
                        .param("title", "My Schedule"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule-sheets/my-schedule"));

        verify(scheduleSheetService, times(1))
                .createSheet("My Schedule");
    }

    @Test
    @DisplayName("POST /schedule-sheets/{slug}/delete はシートを削除して一覧画面へリダイレクトする")
    void deleteRedirectsToScheduleSheetListPage() throws Exception {
        mockMvc.perform(post("/schedule-sheets/my-schedule/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule-sheets"));

        verify(scheduleSheetService, times(1))
                .deleteBySlug("my-schedule");
    }
}