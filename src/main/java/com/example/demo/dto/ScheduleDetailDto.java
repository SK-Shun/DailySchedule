package com.example.demo.dto;

import java.util.List;

public class ScheduleDetailDto {

    private final ScheduleSheetDto sheet;
    private final List<ScheduleEntryDto> schedules;

    public ScheduleDetailDto(ScheduleSheetDto sheet, List<ScheduleEntryDto> schedules) {
        this.sheet = sheet;
        this.schedules = schedules;
    }

    public ScheduleSheetDto getSheet() {
        return sheet;
    }

    public List<ScheduleEntryDto> getSchedules() {
        return schedules;
    }
}