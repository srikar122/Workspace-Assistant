package com.org.workspace.service.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.org.workspace.common.exception.BusinessException;
import com.org.workspace.config.GoogleTokenHolder;
import com.org.workspace.domain.entity.CalendarEventEntity;
import com.org.workspace.dto.response.EventResponseDto;
import com.org.workspace.repository.CalendarEventRepository;
import com.org.workspace.service.CalendarService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final CalendarEventRepository eventRepository;
    private final GoogleTokenHolder tokenHolder;

    @PostConstruct
    public void seedInitialEvent() {
        if (eventRepository.count() == 0) {
            eventRepository.save(CalendarEventEntity.builder()
                    .id(UUID.randomUUID().toString().substring(0, 8))
                    .summary("Architecture Review with Dev Team")
                    .startTime(LocalDateTime.now().plusHours(2))
                    .endTime(LocalDateTime.now().plusHours(3))
                    .attendees("rahul@company.com, sarah@company.com")
                    .build());
            log.info("Seeded default calendar event into PostgreSQL.");
        }
    }

    private Calendar buildGoogleCalendarClient() throws Exception {
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(tokenHolder.getAccessToken(), null));
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("Enterprise Executive Workspace")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDto> getAllEvents() {
        LocalDateTime now = LocalDateTime.now();

        if (tokenHolder.isConnected()) {
            try {
                Calendar service = buildGoogleCalendarClient();
                Events events = service.events().list("primary")
                        .setTimeMin(new DateTime(System.currentTimeMillis()))
                        .setMaxResults(20)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();

                return events.getItems().stream()
                        .map(this::mapGoogleEventToDto)
                        .toList();
            } catch (Exception e) {
                log.error("Failed to fetch from live Google Calendar, falling back to local DB", e);
            }
        }

        // Local fallback: also hides past/finished events so old meetings don't clutter the sidebar
        return eventRepository.findAllByOrderByStartTimeAsc().stream()
                .filter(e -> e.getEndTime().isAfter(now))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventResponseDto scheduleEvent(String summary, String startIso, String endIso, String attendees) {
        LocalDateTime startLocal = parseIsoToLocal(startIso);
        LocalDateTime endLocal = parseIsoToLocal(endIso);
        String eventId = UUID.randomUUID().toString().substring(0, 8);

        if (tokenHolder.isConnected()) {
            try {
                Calendar service = buildGoogleCalendarClient();
                Event googleEvent = new Event().setSummary(summary);

                Date startDate = Date.from(startLocal.atZone(ZoneId.systemDefault()).toInstant());
                Date endDate = Date.from(endLocal.atZone(ZoneId.systemDefault()).toInstant());

                googleEvent.setStart(new EventDateTime()
                        .setDateTime(new DateTime(startDate))
                        .setTimeZone(ZoneId.systemDefault().getId()));
                googleEvent.setEnd(new EventDateTime()
                        .setDateTime(new DateTime(endDate))
                        .setTimeZone(ZoneId.systemDefault().getId()));

                if (attendees != null && !attendees.isBlank()) {
                    List<EventAttendee> attendeeList = Arrays.stream(attendees.split(","))
                            .map(String::trim)
                            .filter(s -> s.contains("@"))
                            .map(email -> new EventAttendee().setEmail(email))
                            .toList();
                    googleEvent.setAttendees(attendeeList);
                }

                Event created = service.events().insert("primary", googleEvent).execute();
                eventId = created.getId();
                log.info("Created live Google Calendar event ID: {}", eventId);
            } catch (Exception e) {
                log.error("Failed to create event in Google Calendar", e);
                throw new BusinessException("Google Calendar Error: " + e.getMessage());
            }
        }

        CalendarEventEntity entity = CalendarEventEntity.builder()
                .id(eventId)
                .summary(summary)
                .startTime(startLocal)
                .endTime(endLocal)
                .attendees(attendees != null ? attendees : "")
                .build();
        return toDto(eventRepository.save(entity));
    }

    @Override
    @Transactional
    public EventResponseDto rescheduleEvent(String identifier, String newStartIso, String newEndIso) {
        LocalDateTime newStart = parseIsoToLocal(newStartIso);
        LocalDateTime newEnd = parseIsoToLocal(newEndIso);

        if (tokenHolder.isConnected()) {
            try {
                Calendar service = buildGoogleCalendarClient();
                Event match = findMatchingGoogleEvent(service, identifier);
                if (match != null) {
                    Date startDate = Date.from(newStart.atZone(ZoneId.systemDefault()).toInstant());
                    Date endDate = Date.from(newEnd.atZone(ZoneId.systemDefault()).toInstant());

                    match.setStart(new EventDateTime()
                            .setDateTime(new DateTime(startDate))
                            .setTimeZone(ZoneId.systemDefault().getId()));
                    match.setEnd(new EventDateTime()
                            .setDateTime(new DateTime(endDate))
                            .setTimeZone(ZoneId.systemDefault().getId()));

                    Event updated = service.events().update("primary", match.getId(), match).execute();
                    log.info("Rescheduled live Google Calendar event: {}", updated.getId());
                    return mapGoogleEventToDto(updated);
                }
            } catch (Exception e) {
                log.error("Failed to reschedule event on Google Calendar", e);
                throw new BusinessException("Google Calendar Reschedule Error: " + e.getMessage());
            }
        }

        CalendarEventEntity event = eventRepository.findAll().stream()
                .filter(e -> e.getId().equalsIgnoreCase(identifier) ||
                             e.getSummary().toLowerCase().contains(identifier.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Event not found matching: " + identifier));

        event.setStartTime(newStart);
        event.setEndTime(newEnd);
        return toDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public boolean cancelEvent(String identifier) {
        boolean deletedFromGoogle = false;

        if (tokenHolder.isConnected()) {
            try {
                Calendar service = buildGoogleCalendarClient();
                Event match = findMatchingGoogleEvent(service, identifier);
                if (match != null) {
                    service.events().delete("primary", match.getId()).execute();
                    deletedFromGoogle = true;
                    log.info("Deleted live Google Calendar event: {}", match.getId());
                }
            } catch (Exception e) {
                log.error("Failed to delete event from Google Calendar", e);
            }
        }

        boolean deletedLocally = eventRepository.findAll().stream()
                .filter(e -> e.getId().equalsIgnoreCase(identifier) ||
                             e.getSummary().toLowerCase().contains(identifier.toLowerCase()))
                .findFirst()
                .map(e -> {
                    eventRepository.delete(e);
                    return true;
                }).orElse(false);

        return deletedFromGoogle || deletedLocally;
    }

    private Event findMatchingGoogleEvent(Calendar service, String identifier) throws Exception {
        Events events = service.events().list("primary")
                .setTimeMin(new DateTime(System.currentTimeMillis() - 86400000L))
                .setSingleEvents(true)
                .execute();

        return events.getItems().stream()
                .filter(e -> e.getId().equalsIgnoreCase(identifier) ||
                             (e.getSummary() != null && e.getSummary().toLowerCase().contains(identifier.toLowerCase())))
                .findFirst()
                .orElse(null);
    }

    private LocalDateTime parseIsoToLocal(String isoString) {
        try {
            return LocalDateTime.parse(isoString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return ZonedDateTime.parse(isoString, DateTimeFormatter.ISO_DATE_TIME)
                    .withZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDateTime();
        }
    }

    private EventResponseDto mapGoogleEventToDto(Event e) {
        DateTime startDt = e.getStart().getDateTime() != null ? e.getStart().getDateTime() : e.getStart().getDate();
        DateTime endDt = e.getEnd().getDateTime() != null ? e.getEnd().getDateTime() : e.getEnd().getDate();

        LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochMilli(startDt.getValue()), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochMilli(endDt.getValue()), ZoneId.systemDefault());

        String attendees = "";
        if (e.getAttendees() != null) {
            attendees = e.getAttendees().stream()
                    .map(EventAttendee::getEmail)
                    .collect(Collectors.joining(", "));
        }
        return new EventResponseDto(
                e.getId().length() > 8 ? e.getId().substring(0, 8) : e.getId(),
                e.getSummary() != null ? e.getSummary() : "(No Title)",
                start,
                end,
                attendees
        );
    }

    private EventResponseDto toDto(CalendarEventEntity e) {
        return new EventResponseDto(e.getId(), e.getSummary(), e.getStartTime(), e.getEndTime(), e.getAttendees());
    }
}