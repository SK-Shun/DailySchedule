package com.example.demo.dto;

import java.util.UUID;

public class ScheduleEntryDto {
    private UUID id;
    private String typeName;
    private String typeLabel;
    private int startMin;
    private int endMin;
    private String startTime;
    private String endTime;
    private String memo;

    public ScheduleEntryDto(
            UUID id,
            String typeName,
            String typeLabel,
            int startMin,
            int endMin,
            String startTime,
            String endTime,
            String memo) {
        this.id = id;
        this.typeName = typeName;
        this.typeLabel = typeLabel;
        this.startMin = startMin;
        this.endMin = endMin;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
    }

    public UUID getId() { return id; }
    public String getTypeName() { return typeName; }
    public String getTypeLabel() { return typeLabel; }
    public int getStartMin() { return startMin; }
    public int getEndMin() { return endMin; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getMemo() { return memo; }
}