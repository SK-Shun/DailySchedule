package com.example.demo.web.support;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.example.demo.entity.TaskType;
import com.example.demo.service.ScheduleQueryService;
import com.example.demo.web.form.ScheduleEntryForm;

@Component
public class ScheduleDetailPageBuilder {

    private final ScheduleQueryService queryService;

    public ScheduleDetailPageBuilder(ScheduleQueryService queryService) {
        this.queryService = queryService;
    }

    public void build(String slug, Model model, ScheduleEntryForm form) {
        var detail = queryService.getDetailBySlug(slug);

        model.addAttribute("sheet", detail.getSheet());
        model.addAttribute("schedules", detail.getSchedules());
        model.addAttribute("form", form);
        model.addAttribute("taskTypes", TaskType.values());
    }
}