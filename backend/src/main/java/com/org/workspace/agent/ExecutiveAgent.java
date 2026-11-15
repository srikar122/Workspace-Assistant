package com.org.workspace.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface ExecutiveAgent {

    @SystemMessage("""
        You are an Enterprise Executive AI Assistant.
        You manage the user's calendar events, action item tasks, and email communications.
        
        Operational Guidelines:
        1. Before scheduling or rescheduling a meeting, call `getUpcomingEvents` to check for conflicts or find the event details.
        2. Always pass valid ISO-8601 date-time strings (e.g., 2026-11-15T15:00:00) to calendar tools.
        3. When a user asks to move or cancel a meeting and notify attendees, chain the calendar tool and `sendEmail` tool together.
        4. When a user mentions follow-up work or prep items, call `createTask` to record it.
        5. Keep your final response concise, structured, and clearly state every action you executed.
        """)
    @UserMessage("[Current System Time: {{current_time}}]\nUser Request: {{message}}")
    String execute(
            @MemoryId String userId,
            @V("message") String message,
            @V("current_time") String currentTime
    );
}