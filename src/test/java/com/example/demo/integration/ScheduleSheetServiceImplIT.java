package com.example.demo.integration;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.entity.ScheduleSheet;
import com.example.demo.repository.ScheduleSheetRepository;
import com.example.demo.service.ScheduleSheetService;
import com.example.demo.service.exception.ScheduleSheetNotFoundException;

@SpringBootTest
@ActiveProfiles("test")
class ScheduleSheetServiceImplIT {

    @Autowired
    private ScheduleSheetService service;

    @Autowired
    private ScheduleSheetRepository repo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE schedule_entry, schedule_sheet RESTART IDENTITY CASCADE");
    }

    @Test
    void createSheet_shouldPersistSheet() {
        ScheduleSheet sheet = service.createSheet("My Plan");

        assertThat(sheet.getId()).isNotNull();
        assertThat(sheet.getTitle()).isEqualTo("My Plan");
        assertThat(sheet.getSlug()).isEqualTo("my-plan");

        assertThat(repo.findBySlug("my-plan")).isPresent();
    }

    @Test
    void createSheet_shouldGenerateUniqueSlug_whenSameTitleExists() {
        service.createSheet("My Plan");

        ScheduleSheet second = service.createSheet("My Plan");

        assertThat(second.getSlug()).isEqualTo("my-plan-1");

        assertThat(repo.existsBySlug("my-plan")).isTrue();
        assertThat(repo.existsBySlug("my-plan-1")).isTrue();
    }

    @Test
    void createSheet_shouldTrimTitle() {
        ScheduleSheet sheet = service.createSheet("  My Plan  ");

        assertThat(sheet.getTitle()).isEqualTo("My Plan");
        assertThat(sheet.getSlug()).isEqualTo("my-plan");
    }

    @Test
    void createSheet_shouldFallbackToSheet_whenSlugBecomesBlank() {
        ScheduleSheet sheet = service.createSheet("!!!");

        assertThat(sheet.getTitle()).isEqualTo("!!!");
        assertThat(sheet.getSlug()).isEqualTo("sheet");
    }

    @Test
    void createSheet_shouldGenerateUniqueFallbackSlug_whenBlankSlugAlreadyExists() {
        service.createSheet("!!!");

        ScheduleSheet second = service.createSheet("???");

        assertThat(second.getSlug()).isEqualTo("sheet-1");
    }

    @Test
    void createSheet_shouldThrowException_whenTitleIsNull() {
        assertThatThrownBy(() -> service.createSheet(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("タイトルは必須です");
    }

    @Test
    void createSheet_shouldThrowException_whenTitleIsBlank() {
        assertThatThrownBy(() -> service.createSheet("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("タイトルは必須です");
    }

    @Test
    void findBySlug_shouldReturnSheet() {
        ScheduleSheet created = service.createSheet("Find Test");

        ScheduleSheet found = service.findBySlug(created.getSlug());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getTitle()).isEqualTo("Find Test");
        assertThat(found.getSlug()).isEqualTo("find-test");
    }

    @Test
    void findBySlug_shouldThrowException_whenSheetDoesNotExist() {
        assertThatThrownBy(() -> service.findBySlug("not-exist"))
                .isInstanceOf(ScheduleSheetNotFoundException.class)
                .hasMessage("スケジュールシートが見つかりません: not-exist");
    }

    @Test
    void deleteBySlug_shouldDeleteExistingSheet() {
        ScheduleSheet sheet = service.createSheet("Delete Test");

        service.deleteBySlug(sheet.getSlug());

        assertThat(repo.existsBySlug(sheet.getSlug())).isFalse();
    }

    @Test
    void deleteBySlug_shouldThrowException_whenSheetDoesNotExist() {
        assertThatThrownBy(() -> service.deleteBySlug("not-exist"))
                .isInstanceOf(ScheduleSheetNotFoundException.class)
                .hasMessage("スケジュールシートが見つかりません: not-exist");
    }

    @Test
    void createSheet_shouldSetCreatedAt_byDatabaseDefault() {
        ScheduleSheet sheet = service.createSheet("Time Test");

        ScheduleSheet found = repo.findBySlug(sheet.getSlug()).orElseThrow();

        assertThat(found.getCreatedAt()).isNotNull();
    }
}