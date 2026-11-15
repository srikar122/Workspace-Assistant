package com.org.workspace.service.impl;

import com.org.workspace.common.exception.BusinessException;
import com.org.workspace.domain.entity.TaskEntity;
import com.org.workspace.domain.enums.TaskStatus;
import com.org.workspace.dto.response.TaskResponseDto;
import com.org.workspace.repository.TaskRepository;
import com.org.workspace.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getAllTasks() {
        // Return only PENDING tasks so completed items never clutter the UI
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING)
                .map(t -> new TaskResponseDto(t.getId(), t.getTitle(), t.getStatus().name()))
                .toList();
    }

    @Override
    @Transactional
    public TaskResponseDto createTask(String title) {
        log.info("Creating new task: {}", title);
        TaskEntity entity = TaskEntity.builder()
                .title(title)
                .status(TaskStatus.PENDING)
                .build();
        TaskEntity saved = taskRepository.save(entity);
        return new TaskResponseDto(saved.getId(), saved.getTitle(), saved.getStatus().name());
    }

    @Override
    @Transactional
    public TaskResponseDto completeTask(Long id) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Task not found with ID: " + id));
        
        // Delete from PostgreSQL so it is permanently removed from Action Items
        taskRepository.delete(task);
        log.info("Completed and removed task ID: {}", id);
        
        return new TaskResponseDto(task.getId(), task.getTitle(), TaskStatus.COMPLETED.name());
    }
}