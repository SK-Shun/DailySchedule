package com.example.demo.service;

import java.util.UUID;

import com.example.demo.entity.ScheduleEntry;
import com.example.demo.entity.TaskType;

public interface ScheduleService {

    ScheduleEntry create(String sheetSlug, TaskType type, int startMin, int endMin, String memo);

    void delete(String sheetSlug, UUID entryId);
}