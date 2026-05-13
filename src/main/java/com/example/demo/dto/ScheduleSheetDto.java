package com.example.demo.dto;

public class ScheduleSheetDto {
    private String title;
    private String slug;

    public ScheduleSheetDto(String title, String slug) {
        this.title = title;
        this.slug = slug;
    }

    public String getTitle() { return title; }
    public String getSlug() { return slug; }
}