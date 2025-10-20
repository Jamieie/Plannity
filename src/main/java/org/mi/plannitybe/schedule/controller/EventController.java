package org.mi.plannitybe.schedule.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mi.plannitybe.schedule.dto.CreateEventRequest;
import org.mi.plannitybe.schedule.dto.EventResponse;
import org.mi.plannitybe.schedule.service.EventService;
import org.mi.plannitybe.user.dto.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
