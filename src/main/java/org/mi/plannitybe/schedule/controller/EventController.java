package org.mi.plannitybe.schedule.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mi.plannitybe.schedule.dto.*;
import org.mi.plannitybe.schedule.service.EventService;
import org.mi.plannitybe.user.dto.CustomUserDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody @Valid CreateEventRequest createEventRequest,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getId();
        EventResponse eventResponse = eventService.createEvent(createEventRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "event", eventResponse,
                "message", "일정이 생성되었습니다."
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEvent(@PathVariable("id") @Min(1) Long eventId,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getId();
        EventResponse eventResponse = eventService.getEvent(eventId, userId);
        return ResponseEntity.ok(eventResponse);
    }

    @GetMapping
    public ResponseEntity<?> getEventsForCalendar(
            @Valid EventCalendarRequest eventCalendarRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        String userId = userDetails.getId();
        List<EventCalendarResponse> events = eventService.getEventsForCalendar(eventCalendarRequest.getFrom(), eventCalendarRequest.getTo(), userId);
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable("id") @Min(1) Long eventId,
                                         @RequestBody @Valid UpdateEventRequest updateEventRequest,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getId();
        EventResponse eventResponse = eventService.updateEvent(eventId, updateEventRequest, userId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "event", eventResponse,
                "message", "일정이 수정되었습니다."
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable("id") @Min(1) Long eventId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getId();
        eventService.deleteEvent(eventId, userId);
        return ResponseEntity.ok(Map.of(
                "message", "일정이 삭제되었습니다."
        ));
    }
}
