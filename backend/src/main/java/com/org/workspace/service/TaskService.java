package com.org.workspace.service;

import com.org.workspace.dto.response.TaskResponseDto;

import java.util.List;

public interface TaskService {
    List<TaskResponseDto> getAllTasks();
    TaskResponseDto createTask(String title);
    TaskResponseDto completeTask(Long id);
}