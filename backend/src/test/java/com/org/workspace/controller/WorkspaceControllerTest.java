package com.org.workspace.controller;

import com.org.workspace.dto.response.TaskResponseDto;
import com.org.workspace.service.CalendarService;
import com.org.workspace.service.OrchestratorService;
import com.org.workspace.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkspaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrchestratorService orchestratorService;

    @MockBean
    private CalendarService calendarService;

    @MockBean
    private TaskService taskService;

    @Test
    void shouldReturnTasksWrappedInApiResponse() throws Exception {
        when(taskService.getAllTasks()).thenReturn(
                List.of(new TaskResponseDto(1L, "Review Q3 Architecture", "PENDING"))
        );

        mockMvc.perform(get("/api/v1/workspace/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Review Q3 Architecture"));
    }

    @Test
    void shouldReturnBadRequestWhenChatMessageIsBlank() throws Exception {
        String invalidJson = """
                {
                    "userId": "default-exec",
                    "message": "   "
                }
                """;

        mockMvc.perform(post("/api/v1/workspace/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("message: Prompt message cannot be empty"));
    }
}