package com.example.demo.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.entity.ScheduleEntry;
import com.example.demo.entity.TaskType;
import com.example.demo.repository.ScheduleRepository;
import com.example.demo.repository.ScheduleSheetRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScheduleFlowIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ScheduleSheetRepository sheetRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();
        sheetRepository.deleteAll();
    }

    @Test
    @DisplayName("シート作成から予定追加、重複エラー、削除までの一連の流れを確認できる")
    void createSheet_addEntry_conflict_deleteEntry_flow() throws Exception {
        String title = "flowplan" + UUID.randomUUID().toString().substring(0, 8);
        String slug = title.toLowerCase();

        mockMvc.perform(get("/schedule-sheets"))
                .andExpect(status().isOk())
                .andExpect(view().name("schedule-sheets"))
                .andExpect(model().attributeExists("sheets"));

        mockMvc.perform(post("/schedule-sheets")
                        .param("title", title))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule-sheets/" + slug));

        assertThat(sheetRepository.findBySlug(slug)).isPresent();

        mockMvc.perform(get("/schedule-sheets/" + slug))
                .andExpect(status().isOk())
                .andExpect(view().name("detail"))
                .andExpect(model().attributeExists("sheet"))
                .andExpect(model().attributeExists("schedules"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("taskTypes"));

        mockMvc.perform(post("/schedule-sheets/" + slug + "/entries")
                        .param("type", "WORK")
                        .param("startTime", "09:00")
                        .param("endTime", "10:00")
                        .param("memo", "朝の作業"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule-sheets/" + slug));

        var entries = scheduleRepository.findBySheet_SlugOrderByStartMinAsc(slug);

        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getType()).isEqualTo(TaskType.WORK);
        assertThat(entries.get(0).getStartMin()).isEqualTo(540);
        assertThat(entries.get(0).getEndMin()).isEqualTo(600);
        assertThat(entries.get(0).getMemo()).isEqualTo("朝の作業");

        mockMvc.perform(post("/schedule-sheets/" + slug + "/entries")
                        .param("type", "WORK")
                        .param("startTime", "09:30")
                        .param("endTime", "10:30")
                        .param("memo", "重複する作業"))
                .andExpect(status().isOk())
                .andExpect(view().name("detail"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "同じ種類の予定が重複しています"));

        assertThat(scheduleRepository.findBySheet_SlugOrderByStartMinAsc(slug)).hasSize(1);

        ScheduleEntry entry = entries.get(0);

        mockMvc.perform(post("/schedule-sheets/" + slug + "/entries/" + entry.getId() + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule-sheets/" + slug));

        assertThat(scheduleRepository.findBySheet_SlugOrderByStartMinAsc(slug)).isEmpty();
    }

    @Test
    @DisplayName("予定追加フォームに入力エラーがある場合は詳細画面に戻る")
    void createEntry_returnsDetail_whenFormHasValidationErrors() throws Exception {
        String title = "validationplan" + UUID.randomUUID().toString().substring(0, 8);
        String slug = title.toLowerCase();

        mockMvc.perform(post("/schedule-sheets")
                        .param("title", title))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/schedule-sheets/" + slug + "/entries")
                        .param("type", "")
                        .param("startTime", "")
                        .param("endTime", "10:00")
                        .param("memo", "入力エラー確認"))
                .andExpect(status().isOk())
                .andExpect(view().name("detail"))
                .andExpect(model().attributeHasFieldErrors("form", "type", "startTime"));

        assertThat(scheduleRepository.findBySheet_SlugOrderByStartMinAsc(slug)).isEmpty();
    }

    @Test
    @DisplayName("同じタイトルのシートを作成するとslugに連番が付く")
    void createSheet_generatesUniqueSlug_whenSameTitleExists() throws Exception {
        String title = "sameflow" + UUID.randomUUID().toString().substring(0, 8);
        String baseSlug = title.toLowerCase();

        mockMvc.perform(post("/schedule-sheets")
                        .param("title", title))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule-sheets/" + baseSlug));

        mockMvc.perform(post("/schedule-sheets")
                        .param("title", title))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule-sheets/" + baseSlug + "-1"));

        assertThat(sheetRepository.findBySlug(baseSlug)).isPresent();
        assertThat(sheetRepository.findBySlug(baseSlug + "-1")).isPresent();
    }

    @Test
    @DisplayName("シートを削除すると一覧画面へリダイレクトされる")
    void deleteSheet_redirectsToIndex() throws Exception {
        String title = "deleteplan" + UUID.randomUUID().toString().substring(0, 8);
        String slug = title.toLowerCase();

        mockMvc.perform(post("/schedule-sheets")
                        .param("title", title))
                .andExpect(status().is3xxRedirection());

        assertThat(sheetRepository.findBySlug(slug)).isPresent();

        mockMvc.perform(post("/schedule-sheets/" + slug + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedule-sheets"));

        assertThat(sheetRepository.findBySlug(slug)).isEmpty();
    }
}