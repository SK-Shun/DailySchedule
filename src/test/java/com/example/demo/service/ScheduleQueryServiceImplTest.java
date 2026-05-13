package com.example.demo.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.dto.ScheduleDetailDto;
import com.example.demo.dto.ScheduleEntryDto;
import com.example.demo.dto.ScheduleSheetDto;
import com.example.demo.entity.ScheduleEntry;
import com.example.demo.entity.ScheduleSheet;
import com.example.demo.entity.TaskType;
import com.example.demo.repository.ScheduleRepository;
import com.example.demo.repository.ScheduleSheetRepository;
import com.example.demo.service.exception.ScheduleSheetNotFoundException;

class ScheduleQueryServiceImplTest {

    private ScheduleSheetRepository sheetRepository;
    private ScheduleRepository scheduleRepository;
    private ScheduleQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        sheetRepository = mock(ScheduleSheetRepository.class);
        scheduleRepository = mock(ScheduleRepository.class);
        service = new ScheduleQueryServiceImpl(sheetRepository, scheduleRepository);
    }

    @Test
    void findAllSheets_returnsDtoList() {
        ScheduleSheet sheet = new ScheduleSheet("タイトル", "slug");

        when(sheetRepository.findAll()).thenReturn(List.of(sheet));

        List<ScheduleSheetDto> result = service.findAllSheets();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("タイトル");
        assertThat(result.get(0).getSlug()).isEqualTo("slug");
    }

    @Test
    void getDetailBySlug_returnsDetailDto() {
        String slug = "test-slug";

        ScheduleSheet sheet = new ScheduleSheet("タイトル", slug);

        ScheduleEntry entry = new ScheduleEntry();
        entry.setSheet(sheet);
        entry.setType(TaskType.WORK);
        entry.setStartMin(60);   
        entry.setEndMin(120);    
        entry.setMemo("メモ");

        when(sheetRepository.findBySlug(slug))
                .thenReturn(Optional.of(sheet));

        when(scheduleRepository.findBySheet_SlugOrderByStartMinAsc(slug))
                .thenReturn(List.of(entry));

        ScheduleDetailDto result = service.getDetailBySlug(slug);

        assertThat(result.getSheet().getTitle()).isEqualTo("タイトル");
        assertThat(result.getSheet().getSlug()).isEqualTo(slug);
        assertThat(result.getSchedules()).hasSize(1);

        ScheduleEntryDto dto = result.getSchedules().get(0);

        assertThat(dto.getTypeName()).isEqualTo("WORK");
        assertThat(dto.getTypeLabel()).isEqualTo("仕事");
        assertThat(dto.getStartMin()).isEqualTo(60);
        assertThat(dto.getEndMin()).isEqualTo(120);
        assertThat(dto.getStartTime()).isEqualTo("01:00");
        assertThat(dto.getEndTime()).isEqualTo("02:00");

        assertThat(dto.getMemo()).isEqualTo("メモ");
    }

    @Test
    void getDetailBySlug_whenNotFound_throwsException() {
        String slug = "not-found";

        when(sheetRepository.findBySlug(slug))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDetailBySlug(slug))
                .isInstanceOf(ScheduleSheetNotFoundException.class)
                .hasMessageContaining(slug);
    }
}