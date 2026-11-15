package com.org.workspace.service;

import com.org.workspace.common.exception.BusinessException;
import com.org.workspace.domain.entity.TaskEntity;
import com.org.workspace.domain.enums.TaskStatus;
import com.org.workspace.dto.response.TaskResponseDto;
import com.org.workspace.repository.TaskRepository;
import com.org.workspace.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void shouldCreatePendingTaskSuccessfully() {
        TaskEntity mockSaved = TaskEntity.builder()
                .id(1L)
                .title("Prepare Q3 Deck")
                .status(TaskStatus.PENDING)
                .build();
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(mockSaved);

        TaskResponseDto result = taskService.createTask("Prepare Q3 Deck");

        assertNotNull(result);
        assertEquals("Prepare Q3 Deck", result.title());
        assertEquals("PENDING", result.status());
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
    }

    @Test
    void shouldThrowBusinessExceptionWhenCompletingNonExistentTask() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> taskService.completeTask(99L));
    }
}