package com.example.demo.advice;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;

import com.example.demo.controller.ScheduleController;
import com.example.demo.controller.ScheduleSheetController;
import com.example.demo.dto.ScheduleSheetDto;
import com.example.demo.entity.TaskType;
import com.example.demo.service.ScheduleQueryService;
import com.example.demo.service.ScheduleService;
import com.example.demo.service.ScheduleSheetService;
import com.example.demo.service.exception.ScheduleConflictException;
import com.example.demo.service.exception.ScheduleEntryNotFoundException;
import com.example.demo.service.exception.ScheduleSheetNotFoundException;
import com.example.demo.web.form.ScheduleEntryForm;
import com.example.demo.web.support.ScheduleDetailPageBuilder;

@WebMvcTest(controllers = {
        ScheduleController.class,
        ScheduleSheetController.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleService scheduleService;

    @MockitoBean
    private ScheduleSheetService scheduleSheetService;

    @MockitoBean
    private ScheduleQueryService scheduleQueryService;

    @MockitoBean
    private ScheduleDetailPageBuilder pageBuilder;

    @BeforeEach
    void setUp() {
        doAnswer(invocation -> {
            Model model = invocation.getArgument(1);
            ScheduleEntryForm form = invocation.getArgument(2);

            model.addAttribute("sheet", new ScheduleSheetDto("Test Sheet", "test-sheet"));
            model.addAttribute("schedules", List.of());
            model.addAttribute("form", form);
            model.addAttribute("taskTypes", TaskType.values());

            return null;
        }).when(pageBuilder).build(any(String.class), any(Model.class), any(ScheduleEntryForm.class));
    }

    @Test
    @DisplayName("ScheduleConflictExceptionの場合、detail画面に戻してerrorMessageを表示する")
    void handleConflict_returnsDetailViewWithErrorMessage() throws Exception {
        doThrow(new ScheduleConflictException("同じ種別の予定が重複しています"))
                .when(scheduleService)
                .create(eq("test-sheet"), eq(TaskType.WORK), eq(540), eq(600), eq("朝の作業"));

        mockMvc.perform(post("/schedule-sheets/test-sheet/entries")
                        .param("type", "WORK")
                        .param("startTime", "09:00")
                        .param("endTime", "10:00")
                        .param("memo", "朝の作業"))
                .andExpect(status().isOk())
                .andExpect(view().name("detail"))
                .andExpect(model().attribute("errorMessage", "同じ種別の予定が重複しています"));

        verify(pageBuilder).build(eq("test-sheet"), any(Model.class), any(ScheduleEntryForm.class));
    }

    @Test
    @DisplayName("IllegalArgumentExceptionの場合、detail画面に戻してerrorMessageを表示する")
    void handleIllegalArgument_returnsDetailViewWithErrorMessage() throws Exception {
        doThrow(new IllegalArgumentException("終了時刻は開始時刻より後にしてください"))
                .when(scheduleService)
                .create(eq("test-sheet"), eq(TaskType.WORK), eq(600), eq(540), eq("逆転"));

        mockMvc.perform(post("/schedule-sheets/test-sheet/entries")
                        .param("type", "WORK")
                        .param("startTime", "10:00")
                        .param("endTime", "09:00")
                        .param("memo", "逆転"))
                .andExpect(status().isOk())
                .andExpect(view().name("detail"))
                .andExpect(model().attribute("errorMessage", "終了時刻は開始時刻より後にしてください"));

        verify(pageBuilder).build(eq("test-sheet"), any(Model.class), any(ScheduleEntryForm.class));
    }

    @Test
    @DisplayName("ScheduleEntryNotFoundExceptionの場合、detail画面に戻してerrorMessageを表示する")
    void handleEntryNotFound_returnsDetailViewWithErrorMessage() throws Exception {
        UUID entryId = UUID.randomUUID();

        doThrow(new ScheduleEntryNotFoundException("予定が見つかりません"))
                .when(scheduleService)
                .delete("test-sheet", entryId);

        mockMvc.perform(post("/schedule-sheets/test-sheet/entries/{entryId}/delete", entryId))
                .andExpect(status().isOk())
                .andExpect(view().name("detail"))
                .andExpect(model().attribute("errorMessage", "予定が見つかりません"));

        verify(pageBuilder).build(eq("test-sheet"), any(Model.class), any(ScheduleEntryForm.class));
    }

    @Test
    @DisplayName("ScheduleSheetNotFoundExceptionの場合、一覧画面へリダイレクトしてflashにerrorMessageを入れる")
    void handleSheetNotFound_redirectsToIndexWithFlashErrorMessage() throws Exception {
        doThrow(new ScheduleSheetNotFoundException("スケジュールシートが見つかりません"))
                .when(scheduleSheetService)
                .deleteBySlug("missing-sheet");

        mockMvc.perform(post("/schedule-sheets/missing-sheet/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule-sheets"))
                .andExpect(flash().attribute("errorMessage", "スケジュールシートが見つかりません"));
    }
}