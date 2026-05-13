package com.example.demo.advice;

import java.util.Map;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.service.exception.ScheduleConflictException;
import com.example.demo.service.exception.ScheduleEntryNotFoundException;
import com.example.demo.service.exception.ScheduleSheetNotFoundException;
import com.example.demo.web.form.ScheduleEntryForm;
import com.example.demo.web.support.ScheduleDetailPageBuilder;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final ScheduleDetailPageBuilder pageBuilder;

    public GlobalExceptionHandler(ScheduleDetailPageBuilder pageBuilder) {
        this.pageBuilder = pageBuilder;
    }

    @ExceptionHandler(ScheduleConflictException.class)
    public String handleConflict(ScheduleConflictException ex, Model model) {
        String slug = extractSlug();
        pageBuilder.build(slug, model, new ScheduleEntryForm());
        model.addAttribute("errorMessage", ex.getMessage());
        return "detail";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        String slug = extractSlug();
        pageBuilder.build(slug, model, new ScheduleEntryForm());
        model.addAttribute("errorMessage", ex.getMessage());
        return "detail";
    }

    @ExceptionHandler(ScheduleEntryNotFoundException.class)
    public String handleEntryNotFound(ScheduleEntryNotFoundException ex, Model model) {
        String slug = extractSlug();
        pageBuilder.build(slug, model, new ScheduleEntryForm());
        model.addAttribute("errorMessage", ex.getMessage());
        return "detail";
    }

    @ExceptionHandler(ScheduleSheetNotFoundException.class)
    public String handleSheetNotFound(ScheduleSheetNotFoundException ex,
                                      RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/schedule-sheets";
    }

    private String extractSlug() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            throw new IllegalStateException("リクエスト情報を取得できません");
        }

        @SuppressWarnings("unchecked")
        Map<String, String> uriTemplateVars =
                (Map<String, String>) attrs.getRequest()
                        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (uriTemplateVars == null || !uriTemplateVars.containsKey("slug")) {
            throw new IllegalStateException("slug を取得できません");
        }

        return uriTemplateVars.get("slug");
    }
}