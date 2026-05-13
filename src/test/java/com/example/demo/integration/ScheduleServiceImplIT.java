package com.example.demo.integration;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.ScheduleEntry;
import com.example.demo.entity.ScheduleSheet;
import com.example.demo.entity.TaskType;
import com.example.demo.repository.ScheduleRepository;
import com.example.demo.repository.ScheduleSheetRepository;
import com.example.demo.service.ScheduleService;
import com.example.demo.service.exception.ScheduleConflictException;
import com.example.demo.service.exception.ScheduleEntryNotFoundException;
import com.example.demo.service.exception.ScheduleSheetNotFoundException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScheduleServiceImplIT {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleSheetRepository sheetRepository;

    @Test
    @DisplayName("create: valid input saves schedule entry")
    void create_shouldSaveEntry_whenValidInput() {
        ScheduleSheet sheet = saveSheet("平日プラン", uniqueSlug("weekday"));

        ScheduleEntry created = scheduleService.create(
                sheet.getSlug(),
                TaskType.WORK,
                540,
                600,
                "  morning work  "
        );

        assertThat(created.getId()).isNotNull();
        assertThat(created.getSheet().getSlug()).isEqualTo(sheet.getSlug());
        assertThat(created.getType()).isEqualTo(TaskType.WORK);
        assertThat(created.getStartMin()).isEqualTo(540);
        assertThat(created.getEndMin()).isEqualTo(600);
        assertThat(created.getMemo()).isEqualTo("morning work");

        ScheduleEntry found = scheduleRepository.findById(created.getId()).orElseThrow();

        assertThat(found.getType()).isEqualTo(TaskType.WORK);
        assertThat(found.getStartMin()).isEqualTo(540);
        assertThat(found.getEndMin()).isEqualTo(600);
        assertThat(found.getMemo()).isEqualTo("morning work");
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("create: same sheet and same type overlapping time throws conflict")
    void create_shouldThrowConflictException_whenSameSheetAndSameTypeOverlaps() {
        ScheduleSheet sheet = saveSheet("休日プラン", uniqueSlug("holiday"));

        scheduleService.create(sheet.getSlug(), TaskType.WORK, 540, 600, "existing");

        assertThatThrownBy(() ->
                scheduleService.create(sheet.getSlug(), TaskType.WORK, 570, 630, "new")
        )
                .isInstanceOf(ScheduleConflictException.class)
                .hasMessage("同じ種類の予定が重複しています");

        assertThat(scheduleRepository.findBySheet_SlugOrderByStartMinAsc(sheet.getSlug()))
                .hasSize(1);
    }

    @Test
    @DisplayName("create: same sheet but different type can overlap")
    void create_shouldAllowOverlap_whenDifferentType() {
        ScheduleSheet sheet = saveSheet("混在プラン", uniqueSlug("mixed"));

        scheduleService.create(sheet.getSlug(), TaskType.WORK, 540, 600, "work");

        ScheduleEntry created = scheduleService.create(
                sheet.getSlug(),
                TaskType.BREAK,
                570,
                630,
                "break"
        );

        assertThat(created.getId()).isNotNull();
        assertThat(created.getType()).isEqualTo(TaskType.BREAK);

        assertThat(scheduleRepository.findBySheet_SlugOrderByStartMinAsc(sheet.getSlug()))
                .hasSize(2);
    }

    @Test
    @DisplayName("create: adjacent time range does not conflict")
    void create_shouldAllowAdjacentTimeRange() {
        ScheduleSheet sheet = saveSheet("隣接プラン", uniqueSlug("adjacent"));

        scheduleService.create(sheet.getSlug(), TaskType.WORK, 540, 600, "first");

        ScheduleEntry created = scheduleService.create(
                sheet.getSlug(),
                TaskType.WORK,
                600,
                660,
                "second"
        );

        assertThat(created.getId()).isNotNull();

        assertThat(scheduleRepository.findBySheet_SlugOrderByStartMinAsc(sheet.getSlug()))
                .hasSize(2);
    }

    @Test
    @DisplayName("create: overlap is checked per sheet")
    void create_shouldAllowSameTypeOverlap_whenDifferentSheet() {
        ScheduleSheet sheetA = saveSheet("プランA", uniqueSlug("plan-a"));
        ScheduleSheet sheetB = saveSheet("プランB", uniqueSlug("plan-b"));

        scheduleService.create(sheetA.getSlug(), TaskType.WORK, 540, 600, "sheet A");

        ScheduleEntry created = scheduleService.create(
                sheetB.getSlug(),
                TaskType.WORK,
                570,
                630,
                "sheet B"
        );

        assertThat(created.getId()).isNotNull();
        assertThat(created.getSheet().getSlug()).isEqualTo(sheetB.getSlug());
    }

    @Test
    @DisplayName("create: null type throws IllegalArgumentException")
    void create_shouldThrowIllegalArgumentException_whenTypeIsNull() {
        ScheduleSheet sheet = saveSheet("型なしプラン", uniqueSlug("null-type"));

        assertThatThrownBy(() ->
                scheduleService.create(sheet.getSlug(), null, 540, 600, "memo")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("予定種別は必須です");
    }

    @Test
    @DisplayName("create: invalid time range throws IllegalArgumentException")
    void create_shouldThrowIllegalArgumentException_whenTimeRangeIsInvalid() {
        ScheduleSheet sheet = saveSheet("不正時間プラン", uniqueSlug("invalid-time"));

        assertThatThrownBy(() ->
                scheduleService.create(sheet.getSlug(), TaskType.WORK, 600, 600, "memo")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("開始は終了より前にしてください");

        assertThatThrownBy(() ->
                scheduleService.create(sheet.getSlug(), TaskType.WORK, -1, 600, "memo")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("時間は 0〜1440 分の範囲で指定してください");

        assertThatThrownBy(() ->
                scheduleService.create(sheet.getSlug(), TaskType.WORK, 540, 1441, "memo")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("時間は 0〜1440 分の範囲で指定してください");
    }

    @Test
    @DisplayName("create: missing sheet throws ScheduleSheetNotFoundException")
    void create_shouldThrowScheduleSheetNotFoundException_whenSheetDoesNotExist() {
        assertThatThrownBy(() ->
                scheduleService.create("missing-sheet", TaskType.WORK, 540, 600, "memo")
        )
                .isInstanceOf(ScheduleSheetNotFoundException.class);
    }

    @Test
    @DisplayName("delete: existing entry on same sheet is deleted")
    void delete_shouldRemoveEntry_whenEntryExistsOnSheet() {
        ScheduleSheet sheet = saveSheet("削除プラン", uniqueSlug("delete"));

        ScheduleEntry entry = scheduleService.create(
                sheet.getSlug(),
                TaskType.WORK,
                540,
                600,
                "delete target"
        );

        scheduleService.delete(sheet.getSlug(), entry.getId());

        assertThat(scheduleRepository.findById(entry.getId())).isEmpty();
    }

    @Test
    @DisplayName("delete: entry on different sheet throws ScheduleEntryNotFoundException")
    void delete_shouldThrowScheduleEntryNotFoundException_whenEntryBelongsToDifferentSheet() {
        ScheduleSheet sheetA = saveSheet("削除元A", uniqueSlug("delete-a"));
        ScheduleSheet sheetB = saveSheet("削除元B", uniqueSlug("delete-b"));

        ScheduleEntry entry = scheduleService.create(
                sheetA.getSlug(),
                TaskType.WORK,
                540,
                600,
                "sheet A entry"
        );

        assertThatThrownBy(() ->
                scheduleService.delete(sheetB.getSlug(), entry.getId())
        )
                .isInstanceOf(ScheduleEntryNotFoundException.class)
                .hasMessage("対象の予定が存在しません");

        assertThat(scheduleRepository.findById(entry.getId())).isPresent();
    }

    @Test
    @DisplayName("delete: missing entry throws ScheduleEntryNotFoundException")
    void delete_shouldThrowScheduleEntryNotFoundException_whenEntryDoesNotExist() {
        ScheduleSheet sheet = saveSheet("存在しない予定削除", uniqueSlug("missing-entry"));

        assertThatThrownBy(() ->
                scheduleService.delete(sheet.getSlug(), UUID.randomUUID())
        )
                .isInstanceOf(ScheduleEntryNotFoundException.class)
                .hasMessage("対象の予定が存在しません");
    }

    private ScheduleSheet saveSheet(String title, String slug) {
        return sheetRepository.saveAndFlush(new ScheduleSheet(title, slug));
    }

    private String uniqueSlug(String base) {
        return base + "-" + UUID.randomUUID();
    }
}