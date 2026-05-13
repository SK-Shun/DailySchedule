package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.service.ScheduleQueryService;
import com.example.demo.service.ScheduleSheetService;

@Controller
@RequestMapping("/schedule-sheets")
public class ScheduleSheetController {

    private final ScheduleSheetService scheduleSheetService;
    private final ScheduleQueryService scheduleQueryService;

    public ScheduleSheetController(ScheduleSheetService scheduleSheetService,
                                   ScheduleQueryService scheduleQueryService) {
        this.scheduleSheetService = scheduleSheetService;
        this.scheduleQueryService = scheduleQueryService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("sheets", scheduleQueryService.findAllSheets());
        return "schedule-sheets";
    }

    @PostMapping
    public String create(@RequestParam String title) {
        String slug = scheduleSheetService.createSheet(title).getSlug();
        return "redirect:/schedule-sheets/" + slug;
    }

    @PostMapping("/{slug}/delete")
    public String delete(@PathVariable String slug) {
        scheduleSheetService.deleteBySlug(slug);
        return "redirect:/schedule-sheets";
    }
}