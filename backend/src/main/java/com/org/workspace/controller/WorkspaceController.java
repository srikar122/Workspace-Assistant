package com.org.workspace.controller;

import com.org.workspace.common.payload.ApiResponse;
import com.org.workspace.config.GoogleTokenHolder;
import com.org.workspace.dto.request.ChatRequestDto;
import com.org.workspace.dto.response.ChatResponseDto;
import com.org.workspace.dto.response.EventResponseDto;
import com.org.workspace.dto.response.TaskResponseDto;
import com.org.workspace.service.CalendarService;
import com.org.workspace.service.OrchestratorService;
import com.org.workspace.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/workspace")
@RequiredArgsConstructor
public class WorkspaceController {

    private final OrchestratorService orchestratorService;
    private final CalendarService calendarService;
    private final TaskService taskService;
    private final GoogleTokenHolder tokenHolder;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponseDto>> sendPrompt(@Valid @RequestBody ChatRequestDto request) {
        ChatResponseDto response = orchestratorService.processUserPrompt(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Agent executed successfully"));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ChatResponseDto>>> getHistory(
            @RequestParam(defaultValue = "default-exec") String userId) {
        return ResponseEntity.ok(
                ApiResponse.ok(orchestratorService.getConversationHistory(userId), "History fetched successfully")
        );
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getEvents() {
        return ResponseEntity.ok(
                ApiResponse.ok(calendarService.getAllEvents(), "Events fetched successfully")
        );
    }

    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<TaskResponseDto>>> getTasks() {
        return ResponseEntity.ok(
                ApiResponse.ok(taskService.getAllTasks(), "Tasks fetched successfully")
        );
    }

    @PatchMapping("/tasks/{id}/complete")
    public ResponseEntity<ApiResponse<TaskResponseDto>> completeTask(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok(taskService.completeTask(id), "Task marked as completed")
        );
    }

@GetMapping("/auth/status")
    public ResponseEntity<ApiResponse<Object>> getAuthStatus() {
        Object status = Map.of(
                "googleConnected", tokenHolder.isConnected(),
                "userEmail", tokenHolder.getUserEmail() != null ? tokenHolder.getUserEmail() : "",
                "userName", tokenHolder.getUserName() != null ? tokenHolder.getUserName() : ""
        );
        return ResponseEntity.ok(ApiResponse.ok(status, "Auth status fetched"));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        tokenHolder.clear();
        return ResponseEntity.ok(ApiResponse.ok(null, "Logged out and cleared Google token"));
    }
}