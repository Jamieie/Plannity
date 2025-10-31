package org.mi.plannitybe.schedule.mapper;

import org.mi.plannitybe.schedule.domain.EventDateTime;
import org.mi.plannitybe.schedule.dto.CreateEventRequest;
import org.mi.plannitybe.schedule.dto.EventResponse;
import org.mi.plannitybe.schedule.entity.Event;
import org.mi.plannitybe.schedule.entity.EventList;
import org.mi.plannitybe.schedule.entity.EventTask;
import org.mi.plannitybe.schedule.entity.Task;

import java.util.stream.Collectors;

public final class EventMapper {
    private EventMapper() {}

    public static Event toEntity(CreateEventRequest request, EventList eventList) {
        EventDateTime eventDateTime = request.eventDateTime();
        return Event.builder()
                .eventList(eventList)
                .title(request.title())
                .startDate(eventDateTime.getStartDate())
                .endDate(eventDateTime.getEndDate())
                .isAllDay(eventDateTime.getIsAllDay())
                .description(request.description())
                .build();
    }

    public static EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .eventListId(event.getEventList().getId())
                .title(event.getTitle())
                .eventDateTime(event.getEventDateTime())
                .description(event.getDescription())
                .taskIds(
                        event.getEventTasks().stream()
                                .map(EventTask::getTask)
                                .map(Task::getId)
                                .collect(Collectors.toList()))
                .build();
    }
}
