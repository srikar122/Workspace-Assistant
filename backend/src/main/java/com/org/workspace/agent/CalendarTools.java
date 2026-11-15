package com.org.workspace.agent;

import com.org.workspace.dto.response.EventResponseDto;
import com.org.workspace.service.CalendarService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarTools {

    private final CalendarService calendarService;

    @Tool("Fetches all upcoming calendar events so you can check the user's schedule and detect time conflicts")
    public String getUpcomingEvents() {
        log.info("[AI Tool Executed] getUpcomingEvents()");
        List<EventResponseDto> events = calendarService.getAllEvents();
        if (events.isEmpty()) {
            return "No upcoming events found on the calendar.";
        }
        StringBuilder sb = new StringBuilder("Upcoming Calendar Events:\n");
        for (EventResponseDto e : events) {
            sb.append(String.format("- ID: %s | Title: %s | Start: %s | End: %s | Attendees: %s\n",
                    e.id(), e.summary(), e.startTime(), e.endTime(), e.attendees()));
        }
        return sb.toString();
    }

    @Tool("Schedules a new meeting. Start and End times MUST be valid ISO-8601 strings (e.g. 2026-11-15T14:00:00)")
    public String scheduleMeeting(
            @P("Title or summary of the meeting") String title,
            @P("Start time in ISO-8601 format") String startIso,
            @P("End time in ISO-8601 format") String endIso,
            @P("Comma-separated attendee email addresses") String attendees) {
        try {
            log.info("[AI Tool Executed] scheduleMeeting: {}", title);
            EventResponseDto dto = calendarService.scheduleEvent(title, startIso, endIso, attendees);
            return "SUCCESS: Scheduled meeting '" + dto.summary() + "' (ID: " + dto.id() + ") for " + dto.startTime();
        } catch (Exception e) {
            return "FAILED to schedule meeting: " + e.getMessage();
        }
    }

    @Tool("Reschedules an existing meeting to a new start and end time using its ID or partial title")
    public String rescheduleMeeting(
            @P("Event ID or partial title of the meeting to move") String idOrTitle,
            @P("New Start time in ISO-8601 format") String startIso,
            @P("New End time in ISO-8601 format") String endIso) {
        try {
            log.info("[AI Tool Executed] rescheduleMeeting: {}", idOrTitle);
            EventResponseDto dto = calendarService.rescheduleEvent(idOrTitle, startIso, endIso);
            return "SUCCESS: Rescheduled '" + dto.summary() + "' to " + dto.startTime() + " - " + dto.endTime();
        } catch (Exception e) {
            return "FAILED to reschedule meeting: " + e.getMessage();
        }
    }

    @Tool("Cancels or deletes an existing meeting given its ID or partial title")
    public String cancelMeeting(@P("Event ID or partial title of the meeting") String idOrTitle) {
        log.info("[AI Tool Executed] cancelMeeting: {}", idOrTitle);
        boolean removed = calendarService.cancelEvent(idOrTitle);
        return removed ? "SUCCESS: Meeting cancelled." : "FAILED: No matching meeting found for '" + idOrTitle + "'.";
    }
}