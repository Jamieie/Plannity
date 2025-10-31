package org.mi.plannitybe.schedule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mi.plannitybe.exception.*;
import org.mi.plannitybe.schedule.dto.CreateEventRequest;
import org.mi.plannitybe.schedule.dto.EventCalendarResponse;
import org.mi.plannitybe.schedule.dto.EventResponse;
import org.mi.plannitybe.schedule.dto.UpdateEventRequest;
import org.mi.plannitybe.schedule.entity.Event;
import org.mi.plannitybe.schedule.entity.EventList;
import org.mi.plannitybe.schedule.entity.EventTask;
import org.mi.plannitybe.schedule.entity.Task;
import org.mi.plannitybe.schedule.mapper.EventMapper;
import org.mi.plannitybe.schedule.repository.EventListRepository;
import org.mi.plannitybe.schedule.repository.EventRepository;
import org.mi.plannitybe.schedule.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EventListRepository eventListRepository;
    private final TaskRepository taskRepository;

    // user의 event를 생성하는 메서드
    @Transactional
    public EventResponse createEvent(CreateEventRequest createEventRequest, String userId) {

        // createEventRequest의 eventListId 유효성 검증 - 존재 여부 및 소유자 일치 여부
        EventList eventList = eventListRepository.findById(createEventRequest.eventListId()).orElseThrow(
                () -> new EventListNotFoundException(userId, createEventRequest.eventListId()));
        if (!userId.equals(eventList.getUser().getId())) {
            throw new EventListAccessDeniedException(userId, createEventRequest.eventListId());
        }

        // tasks에 값이 존재할 경우, task 유효성 검증
        List<Task> tasks = new ArrayList<>();
        if (!createEventRequest.taskIds().isEmpty()) {
            for (Long taskId : createEventRequest.taskIds()) {
                // task 존재 여부 확인
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

        Event event = EventMapper.toEntity(createEventRequest, eventList);  // Event entity 생성
        tasks.forEach(event::addTask);  // event에 task 연관관계 맺어줌
        Event save = eventRepository.save(event);  // event 저장 (영속성 전이 설정으로 eventTask도 함께 저장)

        return EventMapper.toResponse(save);
    }

    // user의 event 단건 상세 조회 메소드
    public EventResponse getEvent(Long eventId, String userId) {
        // 조회 요청한 eventId 유효성 검증
        Event event = eventRepository.findById(eventId).orElseThrow(  // event 존재 여부
                () -> new EventNotFoundException(userId, eventId));
        if (!event.getEventList().getUser().getId().equals(userId)) {  // event 소유자 일치 여부
            throw new EventAccessDeniedException(userId, eventId);
        }

        return EventMapper.toResponse(event);  // 조회한 event 내용 반환
    }

    // user 소유 event 목록 조회 - 캘린더 화면용
    public List<EventCalendarResponse> getEventsForCalendar(LocalDateTime from, LocalDateTime to, String userId) {
        return eventRepository.findEventsByUserIdAndDateRange(userId, from, to);
    }

    // user의 event 업데이트 메소드
    @Transactional
    public EventResponse updateEvent(Long eventId, UpdateEventRequest updateEventRequest, String userId) {

        // 변경 요청한 eventId 유효성 검사
        Event event = eventRepository.findById(eventId).orElseThrow(  // event 존재하지 않으면 예외 발생
                () -> new EventNotFoundException(userId, eventId));
        if (!event.getEventList().getUser().getId().equals(userId)) {  // event의 소유자가 다르면 예외 발생
            throw new EventAccessDeniedException(userId, eventId);
        }

        // 변경 요청한 eventListId의 유효성 검사
        Long requestEventListId = updateEventRequest.eventListId();
        if (requestEventListId != null) {
            EventList eventList = eventListRepository.findById(requestEventListId).orElseThrow(  // eventList 존재하지 않으면 예외 발생
                    () -> new EventListNotFoundException(userId, requestEventListId));
            if (!userId.equals(eventList.getUser().getId())) {  // eventList의 소유자가 다르면 예외 발생
                throw new EventListAccessDeniedException(userId, requestEventListId);
            }
            event.updateEventList(eventList);  // eventList 업데이트
        }

        event.updateTitle(updateEventRequest.title());  // title 업데이트
        event.updateFromEventDateTime(updateEventRequest.eventDateTime());  // eventDateTime 업데이트
        event.updateDescription(updateEventRequest.description());  // description 업데이트

        // 변경 요청한 taskId의 유효성 검사 후 업데이트
        List<Long> requestTaskIds = updateEventRequest.taskIds();
        if (requestTaskIds != null) {
            List<Task> newTasks = new ArrayList<>();
            for (Long taskId : requestTaskIds) {
                Task task = taskRepository.findById(taskId).orElseThrow(  // task 존재하지 않으면 예외 발생
                        () -> new TaskNotFoundException(userId, taskId)
                );
                String taskOwnerId = task.getTaskList().getUser().getId();  // task의 소유자가 다르면 예외 발생
                if (!userId.equals(taskOwnerId)) {
                    throw new TaskAccessDeniedException(userId, taskId);
                }
                newTasks.add(task);
            }

            Set<Long> newTaskIds = newTasks.stream().map(Task::getId).collect(Collectors.toSet());
            Set<Long> currentTaskIds = event.getEventTasks().stream()
                    .map(et -> et.getTask().getId()).collect(Collectors.toSet());

            // 삭제할 EventTask들 (기존에 있었는데 새 목록에 없는 것들)
            List<EventTask> toRemove = event.getEventTasks().stream()
                    .filter(et -> !newTaskIds.contains(et.getTask().getId()))
                    .toList();

            // 추가할 Task들 (새 목록에 있는데 기존에 없던 것들)
            List<Task> toAdd = newTasks.stream()
                    .filter(task -> !currentTaskIds.contains(task.getId()))
                    .toList();

            event.getEventTasks().removeAll(toRemove);  // 삭제
            toAdd.forEach(event::addTask);  // 추가
        }

        Event save = eventRepository.saveAndFlush(event);  // 더티 체킹 하지만 명시적으로 저장 수행

        return EventMapper.toResponse(event);
    }
}
