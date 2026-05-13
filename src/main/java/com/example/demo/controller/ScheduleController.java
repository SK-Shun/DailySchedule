package com.example.demo.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.service.ScheduleService;
import com.example.demo.web.form.ScheduleEntryForm;
import com.example.demo.web.support.ScheduleDetailPageBuilder;

@Controller
@RequestMapping("/schedule-sheets/{slug}")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleDetailPageBuilder pageBuilder;

    public ScheduleController(ScheduleService scheduleService,
                              ScheduleDetailPageBuilder pageBuilder) {
        this.scheduleService = scheduleService;
        this.pageBuilder = pageBuilder;
    }

    @GetMapping
    public String detail(@PathVariable String slug, Model model) {
        pageBuilder.build(slug, model, new ScheduleEntryForm());
        return "detail";
    }

    @PostMapping("/entries")
    public String create(@PathVariable String slug,
                         @Valid @ModelAttribute("form") ScheduleEntryForm form,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            pageBuilder.build(slug, model, form);
            return "detail";
        }

        scheduleService.create(
                slug,
                form.getType(),
                parseTimeToMinutes(form.getStartTime()),
                parseTimeToMinutes(form.getEndTime()),
                form.getMemo()
        );

        return "redirect:/schedule-sheets/" + slug;
    }

    @PostMapping("/entries/{entryId}/delete")
    public String delete(@PathVariable String slug,
                         @PathVariable UUID entryId) {
        scheduleService.delete(slug, entryId);
        return "redirect:/schedule-sheets/" + slug;
    }

    private int parseTimeToMinutes(String time) {
        if (time == null || time.isBlank()) {
            throw new IllegalArgumentException("時刻は必須です");
        }

        String[] parts = time.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("時刻は HH:mm 形式で指定してください");
        }

        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        if (hour < 0 || hour > 24 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("時刻の範囲が不正です");
        }

        if (hour == 24 && minute != 0) {
            throw new IllegalArgumentException("24時は 24:00 のみ指定できます");
        }

        return hour * 60 + minute;
    }
}