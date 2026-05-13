package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;

import com.example.demo.dto.ScheduleSheetDto;
import com.example.demo.entity.TaskType;
import com.example.demo.service.ScheduleService;
import com.example.demo.web.form.ScheduleEntryForm;
import com.example.demo.web.support.ScheduleDetailPageBuilder;

@WebMvcTest(ScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleService scheduleService;

    @MockitoBean
    private ScheduleDetailPageBuilder pageBuilder;

    @Test
    @DisplayName("GET /schedule-sheets/{slug} displays detail page")
    void detail_displaysDetailPage() throws Exception {
        String slug = "my-schedule";

        setupPageBuilderMock(slug);

        mockMvc.perform(get("/schedule-sheets/{slug}", slug))
                .andExpect(status().isOk())
                .andExpect(view().name("detail"))
                .andExpect(model().attributeExists("sheet"))
                .andExpect(model().attributeExists("schedules"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("taskTypes"));

        verify(pageBuilder).build(
                eq(slug),
                any(Model.class),
                any(ScheduleEntryForm.class)
        );
    }

    @Test
    @DisplayName("POST /schedule-sheets/{slug}/entries creates entry and redirects")
    void create_withValidForm_createsEntryAndRedirects() throws Exception {
        String slug = "my-schedule";

        mockMvc.perform(post("/schedule-sheets/{slug}/entries", slug)
                        .param("type", "WORK")
                        .param("startTime", "09:00")
                        .param("endTime", "10:30")
                        .param("memo", "Study Spring Boot"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule-sheets/" + slug));

        verify(scheduleService).create(
                slug,
                TaskType.WORK,
                540,
                630,
                "Study Spring Boot"
        );

        verify(pageBuilder, never()).build(
                any(String.class),
                any(Model.class),
                any(ScheduleEntryForm.class)
        );
    }

    @Test
    @DisplayName("POST /schedule-sheets/{slug}/entries returns detail when validation fails")
    void create_withInvalidForm_returnsDetailPage() throws Exception {
        String slug = "my-schedule";

        setupPageBuilderMock(slug);

        mockMvc.perform(post("/schedule-sheets/{slug}/entries", slug)
                        .param("type", "")
                        .param("startTime", "")
                        .param("endTime", "10:00")
                        .param("memo", "memo"))
                .andExpect(status().isOk())
                .andExpect(view().name("detail"))
                .andExpect(model().attributeHasFieldErrors("form", "type", "startTime"));

        verify(scheduleService, never()).create(
                any(String.class),
                any(TaskType.class),
                any(Integer.class),
                any(Integer.class),
                any(String.class)
        );

        verify(pageBuilder).build(
                eq(slug),
                any(Model.class),
                any(ScheduleEntryForm.class)
        );
    }

    @Test
    @DisplayName("POST /schedule-sheets/{slug}/entries/{entryId}/delete deletes entry and redirects")
    void delete_deletesEntryAndRedirects() throws Exception {
        String slug = "my-schedule";
        UUID entryId = UUID.randomUUID();

        mockMvc.perform(post("/schedule-sheets/{slug}/entries/{entryId}/delete", slug, entryId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule-sheets/" + slug));

        verify(scheduleService).delete(slug, entryId);
    }

    private void setupPageBuilderMock(String slug) {
        doAnswer(invocation -> {
            Model model = invocation.getArgument(1);
            ScheduleEntryForm form = invocation.getArgument(2);

            model.addAttribute("sheet", new ScheduleSheetDto("My Schedule", slug));
            model.addAttribute("schedules", List.of());
            model.addAttribute("form", form);
            model.addAttribute("taskTypes", TaskType.values());

            return null;
        }).when(pageBuilder).build(
                eq(slug),
                any(Model.class),
                any(ScheduleEntryForm.class)
        );
    }
}