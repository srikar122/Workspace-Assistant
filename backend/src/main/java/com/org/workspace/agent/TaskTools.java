package com.org.workspace.agent;

import com.org.workspace.dto.response.TaskResponseDto;
import com.org.workspace.service.TaskService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskTools {

    private final TaskService taskService;

    @Tool("Fetches all tasks and action items on the user's to-do list")
    public String listTasks() {
        log.info("[AI Tool Executed] listTasks()");
        List<TaskResponseDto> tasks = taskService.getAllTasks();
        if (tasks.isEmpty()) {
            return "The task list is currently empty.";
        }
        StringBuilder sb = new StringBuilder("Current Tasks:\n");
        for (TaskResponseDto t : tasks) {
            sb.append(String.format("- ID: %d | Title: %s | Status: %s\n", t.id(), t.title(), t.status()));
        }
        return sb.toString();
    }

    @Tool("Creates a new action item or task on the user's to-do list")
    public String createTask(@P("Clear description or title of the task") String title) {
        log.info("[AI Tool Executed] createTask: {}", title);
        TaskResponseDto dto = taskService.createTask(title);
        return "SUCCESS: Created task ID " + dto.id() + " ('" + dto.title() + "')";
    }

    @Tool("Marks an existing task as COMPLETED given its numeric database ID")
    public String completeTask(@P("Numeric ID of the task to complete") Long id) {
        try {
            log.info("[AI Tool Executed] completeTask: {}", id);
            TaskResponseDto dto = taskService.completeTask(id);
            return "SUCCESS: Marked task '" + dto.title() + "' as COMPLETED.";
        } catch (Exception e) {
            return "FAILED to complete task: " + e.getMessage();
        }
    }
}