package org.mi.plannitybe.schedule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mi.plannitybe.exception.*;
import org.mi.plannitybe.schedule.domain.EventDateTime;
import org.mi.plannitybe.schedule.dto.CreateEventRequest;
import org.mi.plannitybe.schedule.dto.EventCalendarResponse;
import org.mi.plannitybe.schedule.dto.EventResponse;
import org.mi.plannitybe.schedule.entity.Event;
import org.mi.plannitybe.schedule.entity.EventList;
import org.mi.plannitybe.schedule.entity.EventTask;
import org.mi.plannitybe.schedule.entity.Task;
import org.mi.plannitybe.schedule.repository.EventListRepository;
import org.mi.plannitybe.schedule.repository.EventRepository;
import org.mi.plannitybe.schedule.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventListRepository eventListRepository;
    private final TaskRepository taskRepository;

    // user의 event를 생성하는 메서드
    public EventResponse createEvent(CreateEventRequest createEventRequest, String userId) {

        // createEventRequest의 eventListId 유효성 검증 - 존재 여부 및 소유자 일치 여부
        EventList eventList = eventListRepository.findById(createEventRequest.getEventListId()).orElseThrow(
                () -> new EventListNotFoundException(userId, createEventRequest.getEventListId()));
        if (!userId.equals(eventList.getUser().getId())) {
            throw new EventListAccessDeniedException(userId, createEventRequest.getEventListId());
        }

        // 4. tasks에 값이 존재하면 각각의 task는 존재하는 task여야 한다.
        List<Task> tasks = new ArrayList<>();
        if (!createEventRequest.getTaskIds().isEmpty()) {
            for (Long taskId : createEventRequest.getTaskIds()) {
                Task task = taskRepository.findById(taskId).orElseThrow(() ->
                        new TaskNotFoundException(userId, taskId));
                // task의 소유자와 일정 생성 시도하는 사용자가 동일한지 확인
                String taskOwnerId = task.getTaskList().getUser().getId();
                if (!userId.equals(taskOwnerId)) {
                    throw new TaskAccessDeniedException(userId, taskId);
                }
                tasks.add(task); // taskId에 해당하는 task 객체 리스트 추가
            }
        }

        // 5. 위 값으로 Event를 생성한다.
        EventDateTime eventDateTime = createEventRequest.getEventDateTime();
        Event event = Event.builder()
                .eventList(eventList)
                .title(createEventRequest.getTitle())
                .startDate(eventDateTime.getStartDate())
                .endDate(eventDateTime.getEndDate())
                .isAllDay(eventDateTime.getIsAllDay())
                .description(createEventRequest.getDescription())
                .build();

        // 6. event와 task로 eventTask를 생성하여 event 내부의 eventTasks 필드에 추가한다.
        for (Task task : tasks) {
            EventTask eventTask = EventTask.builder()
                    .event(event)
                    .task(task)
                    .build();
            event.addEventTask(eventTask);  // 양방향 연관관계 매핑
        }

        // 7. event를 저장한다. -> 영속성 전이 설정으로 eventTask도 함께 저장된다.
        Event save = eventRepository.save(event);
        return EventResponse.builder()
                .id(save.getId())
                .eventListId(save.getEventList().getId())
                .title(save.getTitle())
                .eventDateTime(save.getEventDateTime())
                .description(save.getDescription())
                .taskIds(
                        save.getEventTasks().stream()
                                .map(EventTask::getTask)
                                .map(Task::getId)
                                .collect(Collectors.toList()))
                .build();
    }

    public EventResponse getEvent(Long eventId, String userId) {
        // 1. eventId로 event 조회 -> 없으면 예외
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new EventNotFoundException(userId, eventId));

        // 2. 조회한 event의 소유자와 로그인한 user가 동일한지 확인 -> 불일치면 예외
        if (!event.getEventList().getUser().getId().equals(userId)) {
            throw new EventAccessDeniedException(userId, eventId);
        }

        // 3. 위 조건 모두 충족하면 조회한 Event 반환
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

    public List<EventCalendarResponse> getEventsForCalendar(LocalDateTime from, LocalDateTime to, String userId) {
        return eventRepository.findEventsByUserIdAndDateRange(userId, from, to);
    }
}
