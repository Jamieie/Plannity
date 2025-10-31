package org.mi.plannitybe.schedule.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mi.plannitybe.exception.*;
import org.mi.plannitybe.schedule.domain.EventDateTime;
import org.mi.plannitybe.schedule.dto.CreateEventRequest;
import org.mi.plannitybe.schedule.dto.EventResponse;
import org.mi.plannitybe.schedule.entity.*;
import org.mi.plannitybe.schedule.repository.EventListRepository;
import org.mi.plannitybe.schedule.repository.EventRepository;
import org.mi.plannitybe.schedule.repository.TaskRepository;
import org.mi.plannitybe.user.entity.User;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventListRepository eventListRepository;

    @Mock
    private TaskRepository taskRepository;

    // 테스트 상수
    private static final String DEFAULT_TITLE = "new event title";
    private static final String DEFAULT_DESCRIPTION = "new event description";
    private static final LocalDateTime DEFAULT_START_DATE = LocalDateTime.of(2025, 1, 1, 10, 0);
    private static final LocalDateTime DEFAULT_END_DATE = LocalDateTime.of(2025, 1, 1, 12, 0);
    private static final Long DEFAULT_EVENT_LIST_ID = 1L;
    
    private String userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
    }

    // ================ createEvent 테스트 ================

    @ParameterizedTest(name = "{index}: 종일일정 = {3} / {0}")
    @CsvSource({
            "'시작날짜와 종료날짜 같음', '2025-01-01T00:00', '2025-01-01T00:00', false",
            "'시작날짜와 종료날짜 1분 차이 ', '2025-01-01T00:00', '2025-01-01T00:01', false",
            "'시작날짜와 종료날짜 23시간 59분 차이', '2025-01-01T00:00', '2025-01-01T23:59', false",
            "'시작날짜와 종료날짜 24시간 차이', '2025-01-01T00:00', '2025-01-02T00:00', false",
            "'시작날짜와 종료날짜 다른 날', '2025-01-01T00:00', '2025-01-02T00:01', false",
            "'시작날짜와 종료날짜 모두 자정, 하루 차이', '2025-01-01T00:00', '2025-01-02T00:00', true",
            "'시작날짜와 종료날짜 모두 자정, 한달 차이', '2025-01-01T00:00', '2025-02-01T00:00', true",
    })
    @DisplayName("createEvent 성공 - Tasks 없음")
    void createEvent_success_withoutTasks(String testDescription, LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay) {
        // GIVEN
        CreateEventRequest createEventRequest = createEventRequest(startDate, endDate, isAllDay, null);
        setupExistingEventList(DEFAULT_EVENT_LIST_ID, userId);
        mockEventRepositorySaveReturns(createMockEvent(1L, userId));

        // WHEN
        eventService.createEvent(createEventRequest, userId);

        // THEN
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());
        Event savedEvent = eventCaptor.getValue();
        verifySavedEvent(savedEvent, createEventRequest);
    }


    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("validDatesAndAllDayValuesWithTasks")
    @DisplayName("createEvent 성공 - Tasks 있음")
    void createEvent_success_withTasks(String testDescription, LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay, List<Long> taskIds) {
        // GIVEN
        CreateEventRequest createEventRequest = createEventRequest(startDate, endDate, isAllDay, taskIds);
        setupExistingEventList(DEFAULT_EVENT_LIST_ID, userId);
        setupExistingTasks(taskIds, userId);
        mockEventRepositorySaveReturns(createMockEvent(1L, userId));

        // WHEN
        eventService.createEvent(createEventRequest, userId);

        // THEN
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());
        Event savedEvent = eventCaptor.getValue();
        verifySavedEvent(savedEvent, createEventRequest);
    }

    @Test
    @DisplayName("createEvent 실패 - EventList가 존재하지 않음")
    void createEvent_fail_eventListNotFound() {
        // GIVEN
        CreateEventRequest createEventRequest = createEventRequest(DEFAULT_START_DATE, DEFAULT_END_DATE, false, null);
        setupNotExistingEventList(DEFAULT_EVENT_LIST_ID);

        // WHEN & THEN
        assertThrows(EventListNotFoundException.class, () -> eventService.createEvent(createEventRequest, userId));
    }

    @Test
    @DisplayName("createEvent 실패 - EventList 소유자가 다름")
    void createEvent_fail_eventListAccessDenied() {
        // GIVEN
        CreateEventRequest createEventRequest = createEventRequest(DEFAULT_START_DATE, DEFAULT_END_DATE, false, null);
        setupExistingEventList(DEFAULT_EVENT_LIST_ID, "different" + userId);

        // WHEN & THEN
        assertThrows(EventListAccessDeniedException.class, () -> eventService.createEvent(createEventRequest, userId));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("validDatesAndAllDayValuesWithTasks")
    @DisplayName("createEvent 실패 - Task가 존재하지 않음")
    void createEvent_fail_taskNotFound(String testDescription, LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay, List<Long> taskIds) {
        // GIVEN
        CreateEventRequest createEventRequest = createEventRequest(DEFAULT_START_DATE, DEFAULT_END_DATE, false, taskIds);
        setupExistingEventList(DEFAULT_EVENT_LIST_ID, userId);
        setupNotExistingTasks(taskIds);

        // WHEN & THEN
        assertThrows(TaskNotFoundException.class, () -> eventService.createEvent(createEventRequest, userId));
    }

    // ================ 헬퍼 메서드 ================
    
    private CreateEventRequest createEventRequest(LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay, List<Long> taskIds) {
        return new CreateEventRequest(
                DEFAULT_EVENT_LIST_ID, DEFAULT_TITLE, EventDateTime.of(startDate, endDate, isAllDay), DEFAULT_DESCRIPTION, taskIds
        );
    }
    
    private void mockEventRepositorySaveReturns(Event event) {
        given(eventRepository.save(any(Event.class))).willReturn(event);
    }

    // eventListId로 저장된 EventList 조회 시 유효한 eventList 객체 반환하도록 설정
    private void setupExistingEventList(Long eventListId, String userId) {
        EventList mockEventList = createMockEventList(eventListId, userId);
        given(eventListRepository.findById(eventListId)).willReturn(Optional.of(mockEventList));
    }

    // eventListId로 저장된 EventList 조회 시 Optional.null 반환하도록 설정 (존재하지 않는 EventList)
    private void setupNotExistingEventList(Long eventListId) {
        given(eventListRepository.findById(eventListId)).willReturn(Optional.empty());
    }

    // mockEventList 객체 생성하여 반환하는 메소드
    private EventList createMockEventList(Long eventListId, String userId) {
        EventList eventList = new EventList();
        eventList.setId(eventListId);
        eventList.setUser(User.builder().id(userId).build());
        return eventList;
    }

    // taskId로 저장된 Task 조회 시 유효한 task 객체 반환하도록 설정
    private void setupExistingTasks(List<Long> taskIds, String userId) {
        List<Task> mockTasks = createMockTasks(taskIds, userId);
        for (int i = 0; i < taskIds.size(); i++) {
            given(taskRepository.findById(taskIds.get(i))).willReturn(Optional.of(mockTasks.get(i)));
        }
    }

    // taskId로 저장된 Task 조회 시 Optional.null 반환하도록 설정 (존재하지 않는 Task)
    private void setupNotExistingTasks(List<Long> taskIds) {
        for (Long taskId : taskIds) {
            given(taskRepository.findById(taskId)).willReturn(Optional.empty());
        }
    }

    // mockTasks 리스트 객체 생성하여 반환하는 메소드
    private List<Task> createMockTasks(List<Long> taskIds, String userId) {
        List<Task> tasks = new ArrayList<>();
        for (Long taskId : taskIds) {
            Task task = new Task();
            task.setId(taskId);
            task.setTaskList(TaskList.builder().user(User.builder().id(userId).build()).build());
            tasks.add(task);
        }
        return tasks;
    }

    // 저장한 Event의 값과 넘겨준 값이 동일한지 확인하는 메소드
    private void verifySavedEvent(Event savedEvent, CreateEventRequest createEventRequest) {
        // 두 객체 내부 필드 각각에 대해 값이 동일한지 확인
        assertThat(savedEvent.getEventList().getId()).isEqualTo(createEventRequest.eventListId());
        assertThat(savedEvent.getTitle()).isEqualTo(createEventRequest.title());
        assertThat(savedEvent.getStartDate()).isEqualTo(createEventRequest.eventDateTime().getStartDate());
        assertThat(savedEvent.getEndDate()).isEqualTo(createEventRequest.eventDateTime().getEndDate());
        assertThat(savedEvent.getIsAllDay()).isEqualTo(createEventRequest.eventDateTime().getIsAllDay());

        // savedEvent의 EventTask 객체의 task id와 createEventRequest에 저장된 taskIds의 각 taskId가 동일한지 확인
        List<EventTask> savedEventTasks = savedEvent.getEventTasks();
        for (int i = 0; i < savedEventTasks.size(); i++) {
            assertThat(savedEventTasks.get(i).getTask().getId()).isEqualTo(createEventRequest.taskIds().get(i));
        }
    }

    // Task가 존재할 때 Task id가 담긴 리스트와 유효한 날짜 케이스 매개변수 제공
    private static Stream<Arguments> validDatesAndAllDayValuesWithTasks() {
        return Stream.of(
                Arguments.of("날짜지정", LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 1, 0, 0), false, List.of(1L)),
                Arguments.of("종일일정", LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 2, 0, 0), true, List.of(1L))
        );
    }

    // ================ getEvent 테스트 ================
    
    @Test
    @DisplayName("getEvent 성공")
    void getEvent_success() {
        // GIVEN
        Long eventId = 1L;
        Event mockEvent = createMockEvent(eventId, userId);
        given(eventRepository.findById(eventId)).willReturn(Optional.of(mockEvent));

        // WHEN
        EventResponse event = eventService.getEvent(eventId, userId);

        // THEN
        assertThat(event).isNotNull();
        assertThat(event.id()).isEqualTo(eventId);
    }

    @Test
    @DisplayName("getEvent 실패 - Event가 존재하지 않음")
    void getEvent_fail_eventNotFound() {
        Long eventId = 1L;
        given(eventRepository.findById(eventId)).willReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventService.getEvent(eventId, userId));
    }

    @Test
    @DisplayName("getEvent 실패 - Event 소유자가 다름")
    void getEvent_fail_accessDenied() {
        Long eventId = 1L;
        Event mockEvent = createMockEvent(eventId, "different" + userId);
        given(eventRepository.findById(eventId)).willReturn(Optional.of(mockEvent));

        assertThrows(EventAccessDeniedException.class, () -> eventService.getEvent(eventId, userId));
    }

    private Event createMockEvent(Long eventId, String userId) {
        return Event.builder()
                .id(eventId)
                .eventList(createMockEventList(DEFAULT_EVENT_LIST_ID, userId))
                .startDate(DEFAULT_START_DATE)
                .endDate(DEFAULT_END_DATE)
                .isAllDay(false)
                .build();
    }
}