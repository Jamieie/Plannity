package org.mi.plannitybe.schedule.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mi.plannitybe.schedule.dto.CreateEventRequest;
import org.mi.plannitybe.schedule.dto.EventResponse;
import org.mi.plannitybe.schedule.service.EventService;
import org.mi.plannitybe.user.dto.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
