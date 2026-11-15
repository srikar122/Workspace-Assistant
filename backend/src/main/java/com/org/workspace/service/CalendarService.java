package com.org.workspace.service;

import com.org.workspace.dto.response.EventResponseDto;

import java.util.List;

public interface CalendarService {
    List<EventResponseDto> getAllEvents();
    EventResponseDto scheduleEvent(String summary, String startIso, String endIso, String attendees);
    EventResponseDto rescheduleEvent(String identifier, String newStartIso, String newEndIso);
    boolean cancelEvent(String identifier);
}