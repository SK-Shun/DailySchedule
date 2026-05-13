package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ScheduleDetailDto;
import com.example.demo.dto.ScheduleEntryDto;
import com.example.demo.dto.ScheduleSheetDto;
import com.example.demo.entity.ScheduleEntry;
import com.example.demo.entity.ScheduleSheet;
import com.example.demo.repository.ScheduleRepository;
import com.example.demo.repository.ScheduleSheetRepository;
import com.example.demo.service.exception.ScheduleSheetNotFoundException;

@Service
@Transactional(readOnly = true)
public class ScheduleQueryServiceImpl implements ScheduleQueryService {

    private final ScheduleSheetRepository sheetRepository;
    private final ScheduleRepository scheduleRepository;

    public ScheduleQueryServiceImpl(ScheduleSheetRepository sheetRepository,
                                    ScheduleRepository scheduleRepository) {
        this.sheetRepository = sheetRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    public List<ScheduleSheetDto> findAllSheets() {
        return sheetRepository.findAll().stream()
                .map(this::toSheetDto)
                .toList();
    }

    @Override
    public ScheduleDetailDto getDetailBySlug(String slug) {
        ScheduleSheet sheet = sheetRepository.findBySlug(slug)
                .orElseThrow(() -> new ScheduleSheetNotFoundException("スケジュールシートが見つかりません: " + slug));

        List<ScheduleEntryDto> schedules = scheduleRepository
        		.findBySheet_SlugOrderByStartMinAsc(slug)
        		.stream()
                .map(this::toEntryDto)
                .toList();

        return new ScheduleDetailDto(
                toSheetDto(sheet),
                schedules
        );
    }

    private ScheduleSheetDto toSheetDto(ScheduleSheet sheet) {
        return new ScheduleSheetDto(
                sheet.getTitle(),
                sheet.getSlug()
        );
    }

    private ScheduleEntryDto toEntryDto(ScheduleEntry entry) {
        return new ScheduleEntryDto(
                entry.getId(),
                entry.getType().name(),
                entry.getType().getLabel(),
                entry.getStartMin(),
                entry.getEndMin(),
                formatTime(entry.getStartMin()),
                formatTime(entry.getEndMin()),
                entry.getMemo()
        );
    }

    private String formatTime(int minutes) {
        int hour = minutes / 60;
        int minute = minutes % 60;
        return String.format("%02d:%02d", hour, minute);
    }
}