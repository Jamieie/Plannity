package org.mi.plannitybe.schedule.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mi.plannitybe.exception.*;
import org.mi.plannitybe.schedule.dto.CreateEventRequest;
import org.mi.plannitybe.schedule.dto.EventResponse;
import org.mi.plannitybe.schedule.entity.Event;
import org.mi.plannitybe.schedule.entity.EventList;
import org.mi.plannitybe.schedule.entity.EventTask;
import org.mi.plannitybe.schedule.entity.Task;
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

    private String userId;
    private Long eventListId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isAllDay;
    private String title;
    private String description;
    private List<Long> taskIds;


    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        eventListId = 1L;
        startDate = null;
        endDate = null;
        isAllDay = false;
        title = "new event title";
        description = "new event description";
        taskIds = null;
    }

    /*
    <event 생성 조건>
    1. eventList가 존재하고 소유자가 요청 userId와 동일해야 한다.
    2. 종료날짜와 시작날짜는 모두 null이거나 모두 존재해야 한다. -> 서비스계층 X, DTO 책임 -> 통합테스트
    3. (날짜가 모두 존재하면) 종료날짜는 시작날짜보다 미래이거나 같아야 한다. -> 서비스계층 X, DTO 책임 -> 통합테스트
    4. 종일일정(isAllDay == true)이라면 종료날짜와 시작날짜는 반드시 값이 존재해야 한다.
    5. 종일일정(isAllDay == true)이라면 종료날짜와 시작날짜가 하루 이상 차이가 나야 한다.
    6. 종일일정(isAllDay == true)이라면 종료날짜와 시작날짜의 시간은 모두 자정이어야 한다.(00:00:00)
    7. tasks에 값이 존재하면 각각의 task는 존재하는 task여야 한다.

    <event 생성 성공 케이스>
    - 회원 userId -> 조회한 EventList 객체의 소유자 id와 동일
    - 존재하는 eventList id -> eventListRepository.findById 호출 시 EventList 객체 반환
    - 조건을 만족하는 event
    1. 시작날짜와 종료날짜 모두 null & 종일일정 false & title 입력 & tasks 없음
    2. 시작날짜와 종료날짜 모두 존재 & 종일일정 false & title 입력 & tasks 없음
    3. 시작날짜와 종료날짜 모두 자정이면서 하루 이상 차이 & 종일일정 true & title 입력 & task 없음
    4. 시작날짜와 종료날짜 모두 null & 종일일정 false & title 입력 & tasks 있음

    <event 생성 실패 케이스>
    1. eventList가 존재하지 않으면 EventListNotFoundException이 발생한다.
    2. eventList의 소유자 userId와 요청 userId가 다르면 UnauthorizedEventListAccessException이 발생한다.
    3. 종일일정(isAllDay == true)이면서 종료날짜와 시작날짜가 null이면 InvalidAllDayEventDateException이 발생한다.
    4. 종일일정(isAllDay == true)이면서 종료날짜와 시작날짜가 하루 이상 차이나지 않으면 InvalidAllDayEventDateException이 발생한다.
    5. 종일일정(isAllDay == true)이면서 종료날짜 또는 시작날짜 시간이 자정이 아니면 InvalidAllDayEventDateException이 발생한다.
    6. task에 값이 존재하는 경우, 각각의 task가 존재하지 않는 task면 TaskNotFoundException이 발생한다.
    */

    @ParameterizedTest(name = "{index}: 종일일정 = {3} / {0}")
    @CsvSource({
            "'시작날짜와 종료날짜 없음', , , false",
            "'시작날짜와 종료날짜 같음', '2025-01-01T00:00', '2025-01-01T00:00', false",
            "'시작날짜와 종료날짜 1분 차이 ', '2025-01-01T00:00', '2025-01-01T00:01', false",
            "'시작날짜와 종료날짜 23시간 59분 차이', '2025-01-01T00:00', '2025-01-01T23:59', false",
            "'시작날짜와 종료날짜 24시간 차이', '2025-01-01T00:00', '2025-01-02T00:00', false",
            "'시작날짜와 종료날짜 다른 날', '2025-01-01T00:00', '2025-01-02T00:01', false",
            "'시작날짜와 종료날짜 모두 자정, 하루 차이', '2025-01-01T00:00', '2025-01-02T00:00', true",
            "'시작날짜와 종료날짜 모두 자정, 한달 차이', '2025-01-01T00:00', '2025-02-01T00:00', true",
    })
    @DisplayName("Event 생성 성공 - Tasks 없음")
    void createEventOk_withoutTasks(String testDescription, LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay) {
        // GIVEN
        CreateEventRequest createEventRequest = new CreateEventRequest(
                eventListId, title, startDate, endDate, isAllDay, description, taskIds
        );
        setupValidEventList(eventListId, userId);
        mockEventRepositorySaveReturnsStubEvent();

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
    @DisplayName("Event 생성 성공 - Tasks 있음")
    void createEventOk_withTasks(String testDescription, LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay, List<Long> taskIds) {
        // GIVEN
        CreateEventRequest createEventRequest = new CreateEventRequest(
                eventListId, title, startDate, endDate, isAllDay, description, taskIds
        );
        setupValidEventList(eventListId, userId);
        setupValidTasks(taskIds);
        mockEventRepositorySaveReturnsStubEvent();

        // WHEN
        eventService.createEvent(createEventRequest, userId);

        // THEN
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();
        verifySavedEvent(savedEvent, createEventRequest);
    }

    // 이벤트 생성 실패 테스트
    @Test
    @DisplayName("1. eventList가 존재하지 않으면 EventListNotFoundException이 발생한다.")
    void createEventFailure_EventListNotFoundException() {
        // GIVEN
        CreateEventRequest createEventRequest = new CreateEventRequest(
                eventListId, title, startDate, endDate, isAllDay, description, taskIds
        );
        setupNotExistingEventList(eventListId);

        // WHEN & THEN
        assertThrows(EventListNotFoundException.class, () -> eventService.createEvent(createEventRequest, userId));
    }

    @Test
    @DisplayName("2. eventList의 소유자 userId와 요청 userId가 다르면 EventListAccessDeniedException이 발생한다.")
    void createEventFailure_unauthorizedEventListAccessException() {
        // GIVEN
        CreateEventRequest createEventRequest = new CreateEventRequest(
                eventListId, title, startDate, endDate, isAllDay, description, taskIds
        );
        setupDifferentUserIdEventList(eventListId, userId);

        // WHEN & THEN
        assertThrows(EventListAccessDeniedException.class, () -> eventService.createEvent(createEventRequest, userId));
    }

    @ParameterizedTest(name = "{index} : {0}")
    @CsvSource({
            "'종일일정이면서 종료날짜와 시작날짜가 null', , , true",
            "'종일일정이면서 종료날짜와 시작날짜의 차이가 하루 미만', '2025-01-01T00:00', '2025-01-01T00:00', true",
            "'종일일정이면서 종료날짜 또는 시작날짜 시간이 자정이 아님', '2025-01-01T01:00', '2025-01-02T01:00', true",
    })
    @DisplayName("3-5. 유효하지 않은 날짜 설정으로 InvalidAllDayEventDateException이 발생")
    void createEventFailure_invalidAllDayEventDateException(String testDescription, LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay) {
        // GIVEN
        CreateEventRequest createEventRequest = new CreateEventRequest(
                eventListId, title, startDate, endDate, isAllDay, description, taskIds
        );
        setupValidEventList(eventListId, userId);

        // WHEN & THEN
        assertThrows(InvalidAllDayEventDateException.class, () -> eventService.createEvent(createEventRequest, userId));
    }

    @ParameterizedTest(name = "{index} : {0}")
    @MethodSource("validDatesAndAllDayValuesWithTasks")
    @DisplayName("6. task에 값이 존재하는 경우, 각각의 task가 존재하지 않는 task면 TaskNotFoundException이 발생한다.")
    void createEventFailure_taskNotFoundException(String testDescription, LocalDateTime startDate, LocalDateTime endDate, Boolean isAllDay, List<Long> taskIds) {
        // GIVEN
        CreateEventRequest createEventRequest = new CreateEventRequest(
                eventListId, title, startDate, endDate, isAllDay, null, taskIds
        );
        setupValidEventList(eventListId, userId);
        setupNotExistingTasks(taskIds);

        // WHEN & THEN
        assertThrows(TaskNotFoundException.class, () -> eventService.createEvent(createEventRequest, userId));
    }

    // Event 저장 후 반환값(Event 객체) 설정
    private void mockEventRepositorySaveReturnsStubEvent() {
        Event event = Event.builder()
                .eventList(new EventList())
                .build();
        given(eventRepository.save(any(Event.class))).willReturn(event);
    }

    // eventListId로 저장된 EventList 조회 시 유효한 eventList 객체 반환하도록 설정
    private void setupValidEventList(Long eventListId, String userId) {
        EventList mockEventList = createMockEventList(eventListId, userId);
        given(eventListRepository.findById(eventListId)).willReturn(Optional.of(mockEventList));
    }

    // eventListId로 저장된 EventList 조회 시 Optional.null 반환하도록 설정 (존재하지 않는 EventList)
    private void setupNotExistingEventList(Long eventListId) {
        given(eventListRepository.findById(eventListId)).willReturn(Optional.empty());
    }

    // eventListId로 저장된 EventList 조회 시 userId가 다른 eventList 객체 반환하도록 설정
    private void setupDifferentUserIdEventList(Long eventListId, String userId) {
        EventList mockEventList = createMockEventList(eventListId, "different" + userId);
        given(eventListRepository.findById(eventListId)).willReturn(Optional.of(mockEventList));
    }

    // mockEventList 객체 생성하여 반환하는 메소드
    private EventList createMockEventList(Long eventListId, String userId) {
        EventList eventList = new EventList();
        eventList.setId(eventListId);
        eventList.setUser(User.builder().id(userId).build());
        return eventList;
    }

    // taskId로 저장된 Task 조회 시 유효한 task 객체 반환하도록 설정
    private void setupValidTasks(List<Long> taskIds) {
        List<Task> mockTasks = createMockTasks(taskIds);
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
    private List<Task> createMockTasks(List<Long> taskIds) {
        List<Task> tasks = new ArrayList<>();
        for (Long taskId : taskIds) {
            Task task = new Task();
            task.setId(taskId);
            tasks.add(task);
        }
        return tasks;
    }

    // 저장한 Event의 값과 넘겨준 값이 동일한지 확인하는 메소드
    private void verifySavedEvent(Event savedEvent, CreateEventRequest createEventRequest) {
        // 두 객체 내부 필드 각각에 대해 값이 동일한지 확인
        assertThat(savedEvent.getEventList().getId()).isEqualTo(createEventRequest.getEventListId());
        assertThat(savedEvent.getTitle()).isEqualTo(createEventRequest.getTitle());
        assertThat(savedEvent.getStartDate()).isEqualTo(createEventRequest.getStartDate());
        assertThat(savedEvent.getEndDate()).isEqualTo(createEventRequest.getEndDate());
        assertThat(savedEvent.getIsAllDay()).isEqualTo(createEventRequest.getIsAllDay());

        // savedEvent의 EventTask 객체의 task id와 createEventRequest에 저장된 taskIds의 각 taskId가 동일한지 확인
        List<EventTask> savedEventTasks = savedEvent.getEventTasks();
        for (int i = 0; i < savedEventTasks.size(); i++) {
            assertThat(savedEventTasks.get(i).getTask().getId()).isEqualTo(createEventRequest.getTaskIds().get(i));
        }
    }

    // Task가 존재할 때 Task id가 담긴 리스트와 유효한 날짜 케이스 매개변수 제공
    private static Stream<Arguments> validDatesAndAllDayValuesWithTasks() {
        return Stream.of(
                Arguments.of("날짜없음", null, null, false, List.of(1L)),
                Arguments.of("날짜지정", LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 1, 0, 0), false, List.of(1L)),
                Arguments.of("종일일정", LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 2, 0, 0), true, List.of(1L))
        );
    }

    // 일정 조회 테스트
    @Test
    @DisplayName("일정 id로 일정 상세 조회 성공")
    void getEvent_success() {
        // GIVEN
        Long eventId = 1L;
        Event mockEvent = createMockEvent(eventId, userId);
        given(eventRepository.findById(eventId)).willReturn(Optional.of(mockEvent));

        // WHEN
        EventResponse event = eventService.getEvent(eventId, userId);

        // THEN
        assertThat(event).isNotNull();
        assertThat(event.getId()).isEqualTo(eventId);
    }

    @Test
    @DisplayName("존재하지 않는 일정 id로 조회 시 EventNotFoundException 발생")
    void getEvent_notFound() {
        Long eventId = 1L;
        given(eventRepository.findById(eventId)).willReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventService.getEvent(eventId, userId));
    }

    @Test
    @DisplayName("소유자가 아닌 userId로 일정 조회 시 EventAccessDeniedException 발생")
    void getEvent_accessDenied() {
        Long eventId = 1L;
        Event mockEvent = createMockEvent(eventId, "different" + userId);
        given(eventRepository.findById(eventId)).willReturn(Optional.of(mockEvent));

        assertThrows(EventAccessDeniedException.class, () -> eventService.getEvent(eventId, userId));
    }

    private Event createMockEvent(Long eventId, String userId) {
        return Event.builder()
                .id(eventId)
                .eventList(createMockEventList(eventListId, userId))
                .build();
    }
}