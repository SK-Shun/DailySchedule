package com.example.demo.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.example.demo.entity.TaskType;

public class ScheduleEntryForm {

    @NotNull(message = "予定種別を選択してください")
    private TaskType type;

    @NotBlank(message = "開始時刻は必須です")
    @Pattern(
        regexp = "^([01]\\d|2[0-4]):[0-5]\\d$",
        message = "開始時刻は HH:mm 形式で入力してください"
    )
    private String startTime;

    @NotBlank(message = "終了時刻は必須です")
    @Pattern(
        regexp = "^([01]\\d|2[0-4]):[0-5]\\d$",
        message = "終了時刻は HH:mm 形式で入力してください"
    )
    private String endTime;

    @Size(max = 500, message = "メモは500文字以内で入力してください")
    private String memo;

    public ScheduleEntryForm() {
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}