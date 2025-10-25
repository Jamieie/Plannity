package org.mi.plannitybe.schedule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mi.plannitybe.exception.*;
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

        // 1. createEventRequest의 eventListId에 해당하는 eventList가 존재해야 한다.
        // 2. 해당 eventList의 userId와 요청을 보낸 userId가 동일해야 한다.
        EventList eventList = eventListRepository.findById(createEventRequest.getEventListId()).orElseThrow(
                () -> new EventListNotFoundException(userId, createEventRequest.getEventListId()));
        if (!userId.equals(eventList.getUser().getId())) {
            throw new EventListAccessDeniedException(userId, createEventRequest.getEventListId());
        }

        // 3. isAllDay가 true면
        // 3-1. startDate와 endDate가 모두 값이 있어야 한다.
        // 3-2. endDate가 startDate보다 하루 이상 커야 한다.
        // 3-3. startDate와 endDate의 시간은 00:00:00이여야 한다.
        if (createEventRequest.getIsAllDay()) {
            if (!createEventRequest.hasBothDates())
                throw new InvalidAllDayEventDateException("종일일정은 날짜가 반드시 지정되어야 합니다.");
            if (!createEventRequest.isStartAtMidnight() || !createEventRequest.isEndAtMidnight())
                throw new InvalidAllDayEventDateException("종일일정은 시작날짜와 종료날짜의 시간을 별도로 설정할 수 없습니다.");
            if (createEventRequest.getDurationInDays() < 1)
                throw new InvalidAllDayEventDateException("종일일정의 날짜는 하루 단위로 설정할 수 있습니다.");
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
        Event event = Event.builder()
                .eventList(eventList)
                .title(createEventRequest.getTitle())
                .startDate(createEventRequest.getStartDate())
                .endDate(createEventRequest.getEndDate())
                .isAllDay(createEventRequest.getIsAllDay())
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
                .startDate(save.getStartDate())
                .endDate(save.getEndDate())
                .isAllDay(save.getIsAllDay())
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
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .isAllDay(event.getIsAllDay())
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
