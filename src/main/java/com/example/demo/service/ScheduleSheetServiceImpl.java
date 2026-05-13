package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.ScheduleSheet;
import com.example.demo.repository.ScheduleSheetRepository;
import com.example.demo.service.exception.ScheduleSheetNotFoundException;

@Service
@Transactional
public class ScheduleSheetServiceImpl implements ScheduleSheetService {

    private final ScheduleSheetRepository repo;

    public ScheduleSheetServiceImpl(ScheduleSheetRepository repo) {
        this.repo = repo;
    }

    @Override
    public ScheduleSheet createSheet(String title) {
        validateTitle(title);

        String normalizedTitle = title.trim();
        String baseSlug = toSlug(normalizedTitle);

        if (baseSlug.isBlank()) {
            baseSlug = "sheet";
        }

        String slug = generateUniqueSlug(baseSlug);

        ScheduleSheet sheet = new ScheduleSheet(normalizedTitle, slug);
        return repo.save(sheet);
    }

    @Override
    public void deleteBySlug(String slug) {
        if (!repo.existsBySlug(slug)) {
            throw new ScheduleSheetNotFoundException("スケジュールシートが見つかりません: " + slug);
        }
        repo.deleteBySlug(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleSheet findBySlug(String slug) {
        return repo.findBySlug(slug)
                .orElseThrow(() -> new ScheduleSheetNotFoundException("スケジュールシートが見つかりません: " + slug));
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("タイトルは必須です");
        }
    }

    private String generateUniqueSlug(String baseSlug) {
        String slug = baseSlug;
        int counter = 1;

        while (repo.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }

    private String toSlug(String title) {
        return title
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}