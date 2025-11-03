package org.mi.plannitybe.schedule.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mi.plannitybe.integration.BaseIntegrationTest;
import org.mi.plannitybe.integration.UserSetUp;
import org.mi.plannitybe.config.JwtTokenProvider;
import org.mi.plannitybe.schedule.entity.Event;
import org.mi.plannitybe.schedule.entity.EventList;
import org.mi.plannitybe.schedule.repository.EventListRepository;
import org.mi.plannitybe.schedule.repository.EventRepository;
import org.mi.plannitybe.user.dto.CustomUserDetails;
import org.mi.plannitybe.user.entity.User;
import org.mi.plannitybe.user.repository.UserRepository;
import org.mi.plannitybe.user.type.UserStatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.mi.plannitybe.schedule.repository.TaskRepository;
import org.mi.plannitybe.schedule.repository.TaskListRepository;
import org.mi.plannitybe.schedule.entity.Task;
import org.mi.plannitybe.schedule.entity.TaskList;
import org.mi.plannitybe.schedule.entity.EventTask;
import org.mi.plannitybe.schedule.type.TaskStatusType;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


class EventControllerIntegrationTest extends BaseIntegrationTest {

    // 테스트 상수
    private static final String DEFAULT_PASSWORD = "password123";
    private static final String DEFAULT_COLOR = "#FF0000";
    private static final String DEFAULT_EVENT_TITLE = "Test Event";
    private static final String DEFAULT_DESCRIPTION = "Test Description";
    private static final String DEFAULT_TASK_TITLE = "Test Task";
    private static final String DEFAULT_TASKLIST_NAME = "Test TaskList";
    private static final String DEFAULT_EVENTLIST_NAME = "Test EventList";
    private static final String TASKLIST_COLOR = "#0000FF";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final LocalDateTime TEST_START_DATE = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
    private static final LocalDateTime TEST_END_DATE = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    // getEventsForCalendar 테스트용 상수
    private static final String DEFAULT_FROM_DATE = "2024-04-01T00:00:00";
    private static final String DEFAULT_TO_DATE = "2024-04-30T00:00:00";

    @Autowired
    private UserSetUp userSetUp;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventListRepository eventListRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskListRepository taskListRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // ================ createEvent 테스트 ================

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'날짜 같음, 종일일정 false', '2024-01-01T10:00:00', '2024-01-01T10:00:00', false, false",
            "'시작날짜 < 종료날짜, 종일일정 false', '2024-01-01T10:00:00', '2024-01-01T12:00:00', false, false",
            "'종일일정 true, 자정, 하루 차이', '2024-01-01T00:00:00', '2024-01-02T00:00:00', true, false",
            "'종일일정 true, 자정, 여러 날 차이', '2024-01-01T00:00:00', '2024-01-05T00:00:00', true, false",
            "'날짜 있음, Tasks 있음', '2024-01-01T10:00:00', '2024-01-01T12:00:00', false, true",
            "'isAllDay null (기본값 false)', '2024-01-01T10:00:00', '2024-01-01T12:00:00', , false"
    })
    @DisplayName("createEvent 성공")
    void createEvent_success(String testDescription, LocalDateTime startDate, LocalDateTime endDate,
                             Boolean isAllDay, Boolean hasTasks) throws Exception {
        // GIVEN - 사용자, EventList, Task 생성
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);

        List<Long> taskIds = null;
        if (hasTasks) {
            Task task = createTask(user);
            taskIds = List.of(task.getId());
        }

        String requestJson = createEventRequestJson(
                eventList.getId(), DEFAULT_EVENT_TITLE,
                startDate != null ? startDate.format(DATE_TIME_FORMATTER) : null,
                endDate != null ? endDate.format(DATE_TIME_FORMATTER) : null,
                isAllDay, taskIds
        );

        // WHEN - POST /events 호출
        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 성공 응답 검증
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("일정이 생성되었습니다."))
                .andExpect(jsonPath("$.event.id").exists())
                .andExpect(jsonPath("$.event.title").value(DEFAULT_EVENT_TITLE))
                .andExpect(jsonPath("$.event.eventListId").value(eventList.getId()))
                .andExpect(jsonPath("$.event.eventDateTime.isAllDay").value(isAllDay != null ? isAllDay : false));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'1초 차이', '2024-01-01T10:00:01', '2024-01-01T10:00:00', false",
            "'1년 차이', '2025-01-01T10:00:00', '2024-01-01T10:00:00', false",
            "'종일일정 1일 차이', '2024-01-02T00:00:00', '2024-01-01T00:00:00', true",
            "'종일일정 1년 차이', '2025-01-01T10:00:00', '2024-01-01T10:00:00', true"
    })
    @DisplayName("createEvent 실패 - 종료날짜가 시작날짜보다 이전")
    void createEvent_fail_invalidDates(String testDescription, LocalDateTime startDate,
                                       LocalDateTime endDate, Boolean isAllDay) throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);

        String requestJson = createEventRequestJson(
                eventList.getId(), DEFAULT_EVENT_TITLE,
                startDate.format(DATE_TIME_FORMATTER),
                endDate.format(DATE_TIME_FORMATTER),
                isAllDay, null
        );

        // WHEN & THEN
        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("eventDateTime.validDateCombination")));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'종일일정이지만 하루 미만 차이', '2024-01-01T00:00:00', '2024-01-01T00:00:00'",
            "'종일일정이지만 시작 시간이 자정 아님', '2024-01-01T01:00:00', '2024-01-02T00:00:00'",
            "'종일일정이지만 종료 시간이 자정 아님', '2024-01-01T00:00:00', '2024-01-02T01:00:00'"
    })
    @DisplayName("createEvent 실패 - 종일일정 날짜 검증")
    void createEvent_fail_allDayDateValidation(String testDescription, LocalDateTime startDate,
                                               LocalDateTime endDate) throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);

        String requestJson = createEventRequestJson(
                eventList.getId(), DEFAULT_EVENT_TITLE,
                startDate.format(DATE_TIME_FORMATTER),
                endDate.format(DATE_TIME_FORMATTER),
                true, null
        );

        // WHEN & THEN
        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("eventDateTime.validAllDayConditions")));
    }

    @Test
    @DisplayName("createEvent 실패 - 존재하지 않는 eventListId")
    void createEvent_fail_eventListNotFound() throws Exception {
        // GIVEN
        User user = createTestUser();
        String accessToken = createJwtToken(user);
        Long nonExistentEventListId = getNonExistentEventListId();

        String requestJson = createEventRequestJson(
                nonExistentEventListId, DEFAULT_EVENT_TITLE, TEST_START_DATE.format(DATE_TIME_FORMATTER), TEST_END_DATE.format(DATE_TIME_FORMATTER), false, null
        );

        // WHEN & THEN
        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_LIST_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("일정리스트가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("createEvent 실패 - 다른 사용자의 eventList")
    void createEvent_fail_eventListNotOwner() throws Exception {
        // GIVEN - 두 명의 사용자 생성
        User owner = createTestUser();
        User otherUser = createTestUser();

        EventList ownerEventList = createEventList(owner, DEFAULT_EVENTLIST_NAME);
        String otherUserToken = createJwtToken(otherUser);

        String requestJson = createEventRequestJson(
                ownerEventList.getId(), DEFAULT_EVENT_TITLE, TEST_START_DATE.format(DATE_TIME_FORMATTER), TEST_END_DATE.format(DATE_TIME_FORMATTER), false, null
        );

        // WHEN & THEN
        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_LIST_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("일정리스트가 존재하지 않습니다."));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'eventListId null', , 'Test Event', '2024-01-01T10:00:00', '2024-01-01T12:00:00', false, 'eventListId'",
            "'title null', '1', , '2024-01-01T10:00:00', '2024-01-01T12:00:00', false, 'title'",
            "'title 빈 문자열', '1', '', '2024-01-01T10:00:00', '2024-01-01T12:00:00', false, 'title'",
            "'title 공백만', '1', '   ', '2024-01-01T10:00:00', '2024-01-01T12:00:00', false, 'title'",
            "'startDate null', '1', 'Test Event', , '2024-01-01T12:00:00', false, 'eventDateTime.startDate'",
            "'endDate null', '1', 'Test Event', '2024-01-01T10:00:00', , false, 'eventDateTime.endDate'"
    })
    @DisplayName("createEvent 실패 - 필수 필드 누락")
    void createEvent_fail_requiredFields(String testDescription, String eventListId,
                                         String title, String startDate, String endDate, String isAllDay, String expectedField) throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);

        // eventListId가 null이 아니면 실제 eventList ID 사용
        Long actualEventListId = eventListId != null ? eventList.getId() : null;
        Boolean actualIsAllDay = isAllDay != null ? Boolean.valueOf(isAllDay) : null;

        String requestJson = createEventRequestJson(
                actualEventListId, title, startDate, endDate, actualIsAllDay, null
        );

        // WHEN & THEN
        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem(expectedField)));
    }

    @Test
    @DisplayName("createEvent 실패 - 존재하지 않는 taskId")
    void createEvent_fail_taskNotFound() throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);

        List<Long> nonExistentTaskIds = List.of(getNonExistentTaskId());
        String requestJson = createEventRequestJson(
                eventList.getId(), DEFAULT_EVENT_TITLE, TEST_START_DATE.format(DATE_TIME_FORMATTER), TEST_END_DATE.format(DATE_TIME_FORMATTER), false, nonExistentTaskIds
        );

        // WHEN & THEN
        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("할일이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("createEvent 실패 - 다른 사용자의 taskId")
    void createEvent_fail_taskNotOwner() throws Exception {
        // GIVEN - 두 명의 사용자 생성
        User taskOwner = createTestUser();
        User eventCreator = createTestUser();

        Task otherUserTask = createTask(taskOwner);
        EventList eventList = createEventList(eventCreator, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(eventCreator);

        List<Long> otherUserTaskIds = List.of(otherUserTask.getId());
        String requestJson = createEventRequestJson(
                eventList.getId(), DEFAULT_EVENT_TITLE, TEST_START_DATE.format(DATE_TIME_FORMATTER), TEST_END_DATE.format(DATE_TIME_FORMATTER), false, otherUserTaskIds
        );

        // WHEN & THEN
        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("할일이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("createEvent 실패 - 인증 없음")
    void createEvent_fail_noAuthentication() throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);

        String requestJson = createEventRequestJson(
                eventList.getId(), DEFAULT_EVENT_TITLE, TEST_START_DATE.format(DATE_TIME_FORMATTER), TEST_END_DATE.format(DATE_TIME_FORMATTER), false, null
        );

        // WHEN & THEN
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("로그인이 필요한 요청입니다."));
    }

    @ValueSource(strings = {
            "{\"eventListId\":1,\"title\":\"Test\",\"eventDateTime\":{\"startDate\":\"invalid-date\",\"endDate\":\"2024-01-01T12:00:00\",\"isAllDay\":false}}",
            "{\"eventListId\":1,\"title\":\"Test\",\"eventDateTime\":{\"startDate\":\"2024-13-01T00:00:00\",\"endDate\":\"2024-01-01T12:00:00\",\"isAllDay\":false}}",
            "{\"eventListId\":1,\"title\":\"Test\",\"eventDateTime\":{\"startDate\":\"2024/01/01 10:00:00\",\"endDate\":\"2024-01-01T12:00:00\",\"isAllDay\":false}}",
            "{\"eventListId\":\"invalid-id\",\"title\":\"Test\",\"eventDateTime\":{\"startDate\":\"2024-01-01T10:00:00\",\"endDate\":\"2024-01-01T12:00:00\",\"isAllDay\":false}}",
            "{\"eventListId\":1,\"title\":\"Test\",\"eventDateTime\":{\"startDate\":\"2024-01-01T10:00:00\",\"endDate\":\"2024-01-01T12:00:00\",\"isAllDay\":\"invalid-allDay\"}}",
            "{\"eventListId\":1,\"title\":\"Test\",\"eventDateTime\":{\"startDate\":\"2024-01-01T10:00:00\",\"endDate\":\"2024-01-01T12:00:00\",\"isAllDay:false}}",
            "{\"eventListId\":1,\"title\":\"Test\",\"eventDateTime\":{\"startDate\":\"2024-01-01T10:00:00\",\"endDate\":\"2024-01-01T12:00:00\",\"isAllDay\"false}}",
            "{\"eventListId\":1,\"title\":\"Test\",\"eventDateTime\":{\"startDate\":\"2024-01-01T10:00:00\",\"endDate\":\"2024-01-01T12:00:00\",isAllDay:false}}",
            "{\"eventListId\":1,\"title\":\"Test\"\"eventDateTime\":{\"startDate\":\"2024-01-01T10:00:00\",\"endDate\":\"2024-01-01T12:00:00\",\"isAllDay\":false}}"
    })
    @ParameterizedTest(name = "{0}")
    @DisplayName("createEvent 실패 - JSON 파싱 불가")
    void createEvent_fail_invalidDateFormat(String invalidJson) throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);

        // JSON에서 eventListId를 실제 값으로 교체
        String requestJson = invalidJson.replace("\"eventListId\":1", "\"eventListId\":" + eventList.getId());

        // WHEN & THEN
        mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.message").value("요청 본문(JSON) 형식이 올바르지 않습니다."));
    }

    // ================ getEvent 테스트 ================

    @Test
    @DisplayName("getEvent 성공 - 존재하는 이벤트 ID와 올바른 소유자로 조회")
    public void getEvent_success() throws Exception {
        // GIVEN - 사용자와 이벤트 데이터 생성
        User user = createTestUser();
        EventList savedEventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        Event savedEvent = createEvent(savedEventList, DEFAULT_EVENT_TITLE, DEFAULT_DESCRIPTION);
        String accessToken = createJwtToken(user);

        // WHEN - GET /events/{id} 호출
        mockMvc.perform(get("/events/{id}", savedEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 성공 응답 검증
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedEvent.getId()))
                .andExpect(jsonPath("$.eventListId").value(savedEventList.getId()))
                .andExpect(jsonPath("$.title").value(savedEvent.getTitle()))
                .andExpect(jsonPath("$.eventDateTime.startDate").value(savedEvent.getStartDate().format(DATE_TIME_FORMATTER)))
                .andExpect(jsonPath("$.eventDateTime.endDate").value(savedEvent.getEndDate().format(DATE_TIME_FORMATTER)))
                .andExpect(jsonPath("$.eventDateTime.isAllDay").value(savedEvent.getIsAllDay()))
                .andExpect(jsonPath("$.description").value(savedEvent.getDescription()))
                .andExpect(jsonPath("$.taskIds").isArray());
    }

    @Test
    @DisplayName("getEvent 실패 - 존재하지 않는 eventId로 일정 조회 시 EVENT_NOT_FOUND 응답")
    public void getEvent_eventNotFound() throws Exception {
        // GIVEN - 사용자 생성
        User user = createTestUser();
        Long nonExistentId = getNonExistentEventId();
        String accessToken = createJwtToken(user);

        // WHEN - GET /events/{id} 호출
        mockMvc.perform(get("/events/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - NOT_FOUND 응답 검증
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("code").value("EVENT_NOT_FOUND"))
                .andExpect(jsonPath("message").value("일정이 존재하지 않습니다."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalidEventId", "one", "-", "9223372036854775808"})
    // 9223372036854775808 = Long의 최대값 + 1
    @DisplayName("getEvent 실패 - 유효하지 않은 데이터 타입의 eventId로 일정 조회 시 응답")
    public void getEvent_invalidTypeEventId(String invalidEventId) throws Exception {
        // GIVEN - 사용자 생성
        User user = createTestUser();
        String accessToken = createJwtToken(user);

        // WHEN - GET /events/{id} 호출
        mockMvc.perform(get("/events/{id}", invalidEventId)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - BAD_REQUEST 응답 검증
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("code").value("INVALID_PARAMETER_TYPE"))
                .andExpect(jsonPath("message").value("요청 파라미터 타입이 올바르지 않습니다."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    @DisplayName("getEvent 실패 - 유효하지 않은 값의 eventId로 일정 조회 시 응답")
    public void getEvent_invalidValueEventId(String invalidEventId) throws Exception {
        // GIVEN - 사용자 생성
        User user = createTestUser();
        String accessToken = createJwtToken(user);

        // WHEN - GET /events/{id} 호출
        mockMvc.perform(get("/events/{id}", invalidEventId)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - BAD_REQUEST 응답 검증
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("eventId")));
    }

    @Test
    @DisplayName("getEvent 실패 - 존재하는 eventId이지만 소유자가 아닌 경우 EVENT_NOT_FOUND 응답")
    public void getEvent_notOwner() throws Exception {
        // GIVEN - 첫 번째 사용자 생성 및 이벤트 생성
        User owner = createTestUser();
        EventList savedEventList = createEventList(owner, DEFAULT_EVENTLIST_NAME);
        Event savedEvent = createEvent(savedEventList, DEFAULT_EVENT_TITLE, DEFAULT_DESCRIPTION);

        // GIVEN - 두 번째 사용자 생성 (이벤트의 소유자가 아님)
        User otherUser = createTestUser();
        String accessToken = createJwtToken(otherUser);

        // WHEN - GET /events/{id} 호출 (소유자가 아닌 사용자로)
        mockMvc.perform(get("/events/{id}", savedEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - NOT_FOUND 응답 검증 (보안상 존재 여부를 알려주지 않음)
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("code").value("EVENT_NOT_FOUND"))
                .andExpect(jsonPath("message").value("일정이 존재하지 않습니다."));
    }

    // 헬퍼 메서드들
    private User createTestUser() {
        String email = UUID.randomUUID() + "@test.com";
        String userId = userSetUp.saveUser(email, DEFAULT_PASSWORD);
        return userRepository.findById(userId).orElseThrow();
    }

    private EventList createEventList(User user, String name) {
        EventList eventList = EventList.builder()
                .user(user)
                .name(name)
                .color(DEFAULT_COLOR)
                .isDefault(true)
                .createdBy(user.getId())
                .updatedBy(user.getId())
                .build();
        return eventListRepository.save(eventList);
    }

    private Event createEvent(EventList eventList, String title, String description) {
        Event event = Event.builder()
                .eventList(eventList)
                .title(title)
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .isAllDay(false)
                .description(description)
                .build();
        return eventRepository.save(event);
    }

    private String createJwtToken(User user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(user.getId())
                .email(user.getEmail())
                .status(UserStatusType.ACTIVE)
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
        return jwtTokenProvider.generateToken(authentication).getAccessToken();
    }

    private Long getNonExistentEventId() {
        return eventRepository.findTopByOrderByIdDesc()
                .map(event -> event.getId() + 1)
                .orElse(1L);
    }

    private Long getNonExistentEventListId() {
        // 현재 시간을 기반으로 존재하지 않을 만한 큰 ID 생성
        return System.currentTimeMillis() + 999999L;
    }

    private Long getNonExistentTaskId() {
        // 현재 시간을 기반으로 존재하지 않을 만한 큰 ID 생성
        return System.currentTimeMillis() + 888888L;
    }

    private TaskList createTaskList(User user, String name) {
        TaskList taskList = TaskList.builder()
                .user(user)
                .name(name)
                .color(TASKLIST_COLOR)
                .isDefault(true)
                .build();
        return taskListRepository.save(taskList);
    }

    private Task createTask(User user) {
        TaskList taskList = createTaskList(user, DEFAULT_TASKLIST_NAME);
        Task task = Task.builder()
                .taskList(taskList)
                .title(DEFAULT_TASK_TITLE)
                .description(DEFAULT_DESCRIPTION)
                .status(TaskStatusType.NOT_STARTED)
                .isAllDay(false)
                .build();
        return taskRepository.save(task);
    }


    // 통합된 JSON 생성 메서드
    private String createEventRequestJson(Long eventListId, String title,
                                          String startDate, String endDate,
                                          Boolean isAllDay, List<Long> taskIds, String description) {
        StringBuilder json = new StringBuilder("{");
        boolean needsComma = false;

        if (eventListId != null) {
            json.append("\"eventListId\":").append(eventListId);
            needsComma = true;
        }
        if (title != null) {
            if (needsComma) json.append(",");
            json.append("\"title\":\"").append(title).append("\"");
            needsComma = true;
        }

        // eventDateTime 객체로 중첩 구조 생성
        boolean hasEventDateTime = (startDate != null || endDate != null || isAllDay != null);
        if (hasEventDateTime) {
            if (needsComma) json.append(",");
            json.append("\"eventDateTime\":{");
            if (startDate != null) {
                json.append("\"startDate\":\"").append(startDate).append("\",");
            }
            if (endDate != null) {
                json.append("\"endDate\":\"").append(endDate).append("\",");
            }
            if (isAllDay != null) {
                json.append("\"isAllDay\":").append(isAllDay);
            } else {
                // 마지막 콤마 제거가 필요한 경우
                if (json.charAt(json.length() - 1) == ',') {
                    json.setLength(json.length() - 1);
                }
            }
            json.append("}");
            needsComma = true;
        }

        if (description != null) {
            if (needsComma) json.append(",");
            json.append("\"description\":\"").append(description).append("\"");
            needsComma = true;
        }

        if (taskIds != null) {
            if (needsComma) json.append(",");
            json.append("\"taskIds\":[");
            if (!taskIds.isEmpty()) {
                json.append(taskIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(""));
            }
            json.append("]");
        }

        json.append("}");
        return json.toString();
    }

    // 기존 메서드들과의 호환성을 위한 오버로드 메서드들
    private String createEventRequestJson(Long eventListId, String title,
                                          String startDate, String endDate,
                                          Boolean isAllDay, List<Long> taskIds) {
        return createEventRequestJson(eventListId, title, startDate, endDate, isAllDay, taskIds, DEFAULT_DESCRIPTION);
    }

    // ================ getEventsForCalendar 테스트 ================

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'기본 범위 내 일정', '2024-04-15T10:00:00', '2024-04-15T12:00:00', 'Event 1', '2024-04-20T14:00:00', '2024-04-20T16:00:00', 'Event 2'",
            "'경계값 - from과 같은 시작시간', '2024-04-01T00:00:00', '2024-04-01T02:00:00', 'Event Starts At From', '2024-04-29T22:00:00', '2024-04-30T00:00:00', 'Event Ends At To'"
    })
    @DisplayName("getEventsForCalendar 성공 - 날짜 범위 내 일정 조회")
    void getEventsForCalendar_success(String testDescription,
                                      String event1Start, String event1End, String event1Title,
                                      String event2Start, String event2End, String event2Title) throws Exception {
        // GIVEN - 사용자, EventList 생성
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);

        // 이벤트 생성
        Event event1 = createEventWithDates(eventList, event1Title,
                LocalDateTime.parse(event1Start),
                LocalDateTime.parse(event1End));
        Event event2 = createEventWithDates(eventList, event2Title,
                LocalDateTime.parse(event2Start),
                LocalDateTime.parse(event2End));

        // WHEN - GET /events 호출
        mockMvc.perform(get("/events")
                        .param("from", DEFAULT_FROM_DATE)
                        .param("to", DEFAULT_TO_DATE)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 성공 응답 검증
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].eventId").value(event1.getId()))
                .andExpect(jsonPath("$[0].title").value(event1Title))
                .andExpect(jsonPath("$[1].eventId").value(event2.getId()))
                .andExpect(jsonPath("$[1].title").value(event2Title));
    }

    @Test
    @DisplayName("getEventsForCalendar 성공 - 시작날짜 기준 정렬")
    void getEventsForCalendar_success_sortByStartDate() throws Exception {
        // GIVEN - 사용자, EventList 생성
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);

        // 정렬 테스트를 위해 늦은 시간부터 생성
        Event eventLater = createEventWithDates(eventList, "Event Later",
                LocalDateTime.of(2024, 4, 20, 14, 0),
                LocalDateTime.of(2024, 4, 20, 16, 0));
        Event eventMiddle = createEventWithDates(eventList, "Event Middle",
                LocalDateTime.of(2024, 4, 15, 10, 0),
                LocalDateTime.of(2024, 4, 15, 12, 0));
        Event eventEarlier = createEventWithDates(eventList, "Event Earlier",
                LocalDateTime.of(2024, 4, 5, 9, 0),
                LocalDateTime.of(2024, 4, 5, 11, 0));

        // WHEN - GET /events 호출
        mockMvc.perform(get("/events")
                        .param("from", DEFAULT_FROM_DATE)
                        .param("to", DEFAULT_TO_DATE)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 시작날짜 기준 오름차순 정렬 확인
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].eventId").value(eventEarlier.getId()))
                .andExpect(jsonPath("$[0].title").value("Event Earlier"))
                .andExpect(jsonPath("$[0].eventDateTime.startDate").value("2024-04-05T09:00:00"))
                .andExpect(jsonPath("$[1].eventId").value(eventMiddle.getId()))
                .andExpect(jsonPath("$[1].title").value("Event Middle"))
                .andExpect(jsonPath("$[1].eventDateTime.startDate").value("2024-04-15T10:00:00"))
                .andExpect(jsonPath("$[2].eventId").value(eventLater.getId()))
                .andExpect(jsonPath("$[2].title").value("Event Later"))
                .andExpect(jsonPath("$[2].eventDateTime.startDate").value("2024-04-20T14:00:00"));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'from과 to가 동일', '2024-04-15T10:00:00', '2024-04-15T10:00:00'",
            "'하루 차이', '2024-04-15T00:00:00', '2024-04-16T00:00:00'",
            "'일주일 차이', '2024-04-01T00:00:00', '2024-04-08T00:00:00'",
            "'한 달 차이', '2024-04-01T00:00:00', '2024-05-01T00:00:00'"
    })
    @DisplayName("getEventsForCalendar 성공 - 다양한 날짜 범위 조합")
    void getEventsForCalendar_success_variousDateRanges(String testDescription, String fromParam, String toParam) throws Exception {
        // GIVEN - 사용자, EventList 생성
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);

        // 범위 내에 포함될 이벤트 생성 (모든 테스트 케이스에서 조회될 수 있도록)
        Event eventInRange = createEventWithDates(eventList, "Event In Range",
                LocalDateTime.parse(fromParam),  // from과 동일한 시간에 시작
                LocalDateTime.parse(fromParam)); // to와 동일한 시간에 종료

        // WHEN - GET /events 호출
        mockMvc.perform(get("/events")
                        .param("from", fromParam)
                        .param("to", toParam)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 성공 응답 검증
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventId").value(eventInRange.getId()))
                .andExpect(jsonPath("$[0].title").value("Event In Range"))
                .andExpect(jsonPath("$[0].eventDateTime.isAllDay").value(false));
    }

    @Test
    @DisplayName("getEventsForCalendar 성공 - 빈 결과")
    void getEventsForCalendar_success_emptyResult() throws Exception {
        // GIVEN - 사용자만 생성 (이벤트 없음)
        User user = createTestUser();
        String accessToken = createJwtToken(user);

        // WHEN - GET /events 호출
        mockMvc.perform(get("/events")
                        .param("from", DEFAULT_FROM_DATE)
                        .param("to", DEFAULT_TO_DATE)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 빈 배열 응답
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("getEventsForCalendar 성공 - 범위 밖 일정은 제외")
    void getEventsForCalendar_success_excludeOutOfRangeEvents() throws Exception {
        // GIVEN - 사용자, EventList 생성
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);

        // 범위 내 이벤트
        Event eventInRange = createEventWithDates(eventList, "Event In Range",
                LocalDateTime.of(2024, 4, 15, 10, 0),
                LocalDateTime.of(2024, 4, 15, 12, 0));

        // 범위 밖 이벤트들 (조회되지 않음을 확인하기 위해 생성)
        createEventWithDates(eventList, "Event Before Range",
                LocalDateTime.of(2024, 3, 15, 10, 0),
                LocalDateTime.of(2024, 3, 15, 12, 0));

        createEventWithDates(eventList, "Event After Range",
                LocalDateTime.of(2024, 5, 15, 10, 0),
                LocalDateTime.of(2024, 5, 15, 12, 0));

        // WHEN - GET /events 호출
        mockMvc.perform(get("/events")
                        .param("from", DEFAULT_FROM_DATE)
                        .param("to", DEFAULT_TO_DATE)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 범위 내 이벤트만 조회
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventId").value(eventInRange.getId()))
                .andExpect(jsonPath("$[0].title").value("Event In Range"));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'from 파라미터 누락', 'to', '2024-04-30T00:00:00', 'from'",
            "'to 파라미터 누락', 'from', '2024-04-01T00:00:00', 'to'"
    })
    @DisplayName("getEventsForCalendar 실패 - 필수 파라미터 누락")
    void getEventsForCalendar_fail_missingRequiredParameter(String testDescription,
                                                            String paramName, String paramValue,
                                                            String expectedMissingField) throws Exception {
        // GIVEN
        User user = createTestUser();
        String accessToken = createJwtToken(user);

        // WHEN & THEN
        mockMvc.perform(get("/events")
                        .param(paramName, paramValue)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem(expectedMissingField)));
    }

    @Test
    @DisplayName("getEventsForCalendar 실패 - from이 to보다 미래")
    void getEventsForCalendar_fail_fromAfterTo() throws Exception {
        // GIVEN
        User user = createTestUser();
        String accessToken = createJwtToken(user);

        // WHEN & THEN
        mockMvc.perform(get("/events")
                        .param("from", "2024-04-30T00:00:00")
                        .param("to", "2024-04-01T00:00:00")
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("validRange")));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "invalid-date",
            "2024-13-01T00:00:00",
            "2024/04/01 10:00:00",
            "2024-04-01 10:00:00"
    })
    @DisplayName("getEventsForCalendar 실패 - 잘못된 날짜 형식")
    void getEventsForCalendar_fail_invalidDateFormat(String invalidDate) throws Exception {
        // GIVEN
        User user = createTestUser();
        String accessToken = createJwtToken(user);

        // WHEN & THEN
        mockMvc.perform(get("/events")
                        .param("from", invalidDate)
                        .param("to", "2024-04-30T00:00:00")
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("from"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("from 필드의 날짜 형식이 올바르지 않습니다. (예: 2024-04-01T10:00:00)"));
    }

    @Test
    @DisplayName("getEventsForCalendar 실패 - 인증 없음")
    void getEventsForCalendar_fail_noAuthentication() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/events")
                        .param("from", DEFAULT_FROM_DATE)
                        .param("to", DEFAULT_TO_DATE)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("getEventsForCalendar 성공 - 날짜 없는 일정은 제외")
    void getEventsForCalendar_success_excludeEventsWithoutDates() throws Exception {
        // GIVEN - 사용자, EventList 생성
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);

        // 날짜 있는 이벤트와 날짜 없는 이벤트 생성
        Event eventWithDate = createEventWithDates(eventList, "Event With Date",
                LocalDateTime.of(2024, 4, 15, 10, 0),
                LocalDateTime.of(2024, 4, 15, 12, 0));
        // 날짜 없는 이벤트 (조회되지 않음을 확인하기 위해 생성)
        createEvent(eventList, "Event Without Date", DEFAULT_DESCRIPTION);

        // WHEN - GET /events 호출
        mockMvc.perform(get("/events")
                        .param("from", DEFAULT_FROM_DATE)
                        .param("to", DEFAULT_TO_DATE)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 날짜 있는 이벤트만 조회
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventId").value(eventWithDate.getId()))
                .andExpect(jsonPath("$[0].title").value("Event With Date"));
    }

    // ================ updateEvent 테스트 ================

    @Test
    @DisplayName("updateEvent 성공 - 기존 EventTask 모두 제거")
    void updateEvent_success_removeTasks() throws Exception {
        // GIVEN - 사용자, EventList, Task, Event 생성
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        Task task1 = createTask(user);
        Task task2 = createTask(user);
        Event existingEvent = createEventWithTasks(eventList, "Event with Tasks", List.of(task1.getId(), task2.getId()));
        String accessToken = createJwtToken(user);

        String requestJson = createEventRequestJson(
                eventList.getId(), "Updated Title",
                TEST_START_DATE.format(DATE_TIME_FORMATTER),
                TEST_END_DATE.format(DATE_TIME_FORMATTER),
                false, List.of(), "Updated Description" // 빈 리스트로 모든 Task 제거
        );

        // WHEN - PUT /events/{id} 호출
        mockMvc.perform(put("/events/{id}", existingEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 성공 응답 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.taskIds").isArray())
                .andExpect(jsonPath("$.event.taskIds.length()").value(0));

        // 실제 DB 변경 검증 - GET 요청으로 재조회
        mockMvc.perform(get("/events/{id}", existingEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskIds").isArray())
                .andExpect(jsonPath("$.taskIds.length()").value(0));
    }

    @Test
    @DisplayName("updateEvent 성공 - 기존 EventTask 삭제 후 새 Task 연결 추가")
    void updateEvent_success_replaceTasks() throws Exception {
        // GIVEN - 사용자, EventList, Task들, Event 생성
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        Task oldTask1 = createTask(user);
        Task oldTask2 = createTask(user);
        Task newTask1 = createTask(user);
        Task newTask2 = createTask(user);
        Event existingEvent = createEventWithTasks(eventList, "Event with Old Tasks", List.of(oldTask1.getId(), oldTask2.getId()));
        String accessToken = createJwtToken(user);

        String requestJson = createEventRequestJson(
                eventList.getId(), "Updated Title",
                TEST_START_DATE.format(DATE_TIME_FORMATTER),
                TEST_END_DATE.format(DATE_TIME_FORMATTER),
                false, List.of(newTask1.getId(), newTask2.getId()), "Updated Description" // 새로운 Task들로 교체
        );

        // WHEN - PUT /events/{id} 호출
        mockMvc.perform(put("/events/{id}", existingEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 성공 응답 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.taskIds").isArray())
                .andExpect(jsonPath("$.event.taskIds.length()").value(2))
                .andExpect(jsonPath("$.event.taskIds[*]", hasItem(newTask1.getId().intValue())))
                .andExpect(jsonPath("$.event.taskIds[*]", hasItem(newTask2.getId().intValue())));

        // 실제 DB 변경 검증 - GET 요청으로 재조회
        mockMvc.perform(get("/events/{id}", existingEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskIds").isArray())
                .andExpect(jsonPath("$.taskIds.length()").value(2))
                .andExpect(jsonPath("$.taskIds[*]", hasItem(newTask1.getId().intValue())))
                .andExpect(jsonPath("$.taskIds[*]", hasItem(newTask2.getId().intValue())))
                .andExpect(jsonPath("$.taskIds[*]", not(hasItem(oldTask1.getId().intValue()))))
                .andExpect(jsonPath("$.taskIds[*]", not(hasItem(oldTask2.getId().intValue()))));
    }

    @Test
    @DisplayName("updateEvent 성공 - 기존 EventTask에 새 Task 연결 추가")
    void updateEvent_success_addTasksToExisting() throws Exception {
        // GIVEN - 사용자, EventList, Task들, Event 생성
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        Task existingTask = createTask(user);
        Task newTask1 = createTask(user);
        Task newTask2 = createTask(user);
        Event existingEvent = createEventWithTasks(eventList, "Event with Existing Task", List.of(existingTask.getId()));
        String accessToken = createJwtToken(user);

        String requestJson = createEventRequestJson(
                eventList.getId(), "Updated Title",
                TEST_START_DATE.format(DATE_TIME_FORMATTER),
                TEST_END_DATE.format(DATE_TIME_FORMATTER),
                false, List.of(existingTask.getId(), newTask1.getId(), newTask2.getId()), "Updated Description" // 기존 + 새 Task들
        );

        // WHEN - PUT /events/{id} 호출
        mockMvc.perform(put("/events/{id}", existingEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 성공 응답 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.taskIds").isArray())
                .andExpect(jsonPath("$.event.taskIds.length()").value(3))
                .andExpect(jsonPath("$.event.taskIds[*]", hasItem(existingTask.getId().intValue())))
                .andExpect(jsonPath("$.event.taskIds[*]", hasItem(newTask1.getId().intValue())))
                .andExpect(jsonPath("$.event.taskIds[*]", hasItem(newTask2.getId().intValue())));

        // 실제 DB 변경 검증 - GET 요청으로 재조회
        mockMvc.perform(get("/events/{id}", existingEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskIds").isArray())
                .andExpect(jsonPath("$.taskIds.length()").value(3))
                .andExpect(jsonPath("$.taskIds[*]", hasItem(existingTask.getId().intValue())))
                .andExpect(jsonPath("$.taskIds[*]", hasItem(newTask1.getId().intValue())))
                .andExpect(jsonPath("$.taskIds[*]", hasItem(newTask2.getId().intValue())));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'모든 필드 수정', true, 'Updated Title', 'Updated Description', '2024-03-01T00:00:00', '2024-03-02T00:00:00', true, true",
            "'title만 수정', false, 'Updated Title Only', , , , , false",
            "'description만 수정', false, , 'Updated Description Only', , , , false",
            "'eventDateTime만 수정', false, , , '2024-03-01T00:00:00', '2024-03-02T00:00:00', true, false"
    })
    @DisplayName("updateEvent 성공 - 필드별 수정")
    void updateEvent_success_fieldUpdates(String testDescription, boolean updateAll, String newTitle,
                                          String newDescription, String newStartDate, String newEndDate,
                                          Boolean newIsAllDay, boolean updateTasks) throws Exception {
        // GIVEN - 기존 Event 생성
        User user = createTestUser();
        EventList oldEventList = createEventList(user, "Old EventList");
        EventList newEventList = createEventList(user, "New EventList");
        Task oldTask = createTask(user);
        Task newTask = createTask(user);
        Event existingEvent = createEventWithTasks(oldEventList, "Original Title", List.of(oldTask.getId()));
        String accessToken = createJwtToken(user);

        // 원본 값들 저장
        String originalTitle = existingEvent.getTitle();
        String originalDescription = existingEvent.getDescription();
        LocalDateTime originalStartDate = existingEvent.getStartDate();
        LocalDateTime originalEndDate = existingEvent.getEndDate();
        Boolean originalIsAllDay = existingEvent.getIsAllDay();
        Long originalEventListId = existingEvent.getEventList().getId();

        // 요청 JSON 생성
        Long eventListId = updateAll ? newEventList.getId() : null;
        List<Long> taskIds = updateTasks ? List.of(newTask.getId()) : null;

        String requestJson = createEventRequestJson(
                eventListId, newTitle, newStartDate, newEndDate, newIsAllDay, taskIds, newDescription
        );

        // WHEN - PUT /events/{id} 호출
        mockMvc.perform(put("/events/{id}", existingEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 예상 값과 실제 값 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.eventListId").value(updateAll ? newEventList.getId() : originalEventListId))
                .andExpect(jsonPath("$.event.title").value(newTitle != null ? newTitle : originalTitle))
                .andExpect(jsonPath("$.event.description").value(newDescription != null ? newDescription : originalDescription))
                .andExpect(jsonPath("$.event.eventDateTime.startDate").value(
                        newStartDate != null ? newStartDate : originalStartDate.format(DATE_TIME_FORMATTER)))
                .andExpect(jsonPath("$.event.eventDateTime.endDate").value(
                        newEndDate != null ? newEndDate : originalEndDate.format(DATE_TIME_FORMATTER)))
                .andExpect(jsonPath("$.event.eventDateTime.isAllDay").value(
                        newIsAllDay != null ? newIsAllDay : originalIsAllDay))
                .andExpect(jsonPath("$.event.taskIds.length()").value(1))
                .andExpect(jsonPath("$.event.taskIds[0]").value(updateTasks ? newTask.getId() : oldTask.getId()));

        // "모든 필드 수정" 케이스에서만 실제 DB 변경 검증
        if (updateAll) {
            mockMvc.perform(get("/events/{id}", existingEvent.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eventListId").value(newEventList.getId()))
                    .andExpect(jsonPath("$.title").value(newTitle))
                    .andExpect(jsonPath("$.description").value(newDescription))
                    .andExpect(jsonPath("$.eventDateTime.startDate").value(newStartDate))
                    .andExpect(jsonPath("$.eventDateTime.endDate").value(newEndDate))
                    .andExpect(jsonPath("$.eventDateTime.isAllDay").value(newIsAllDay))
                    .andExpect(jsonPath("$.taskIds.length()").value(1))
                    .andExpect(jsonPath("$.taskIds[0]").value(newTask.getId()));
        }
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'빈 문자열', ''",
            "'공백 문자열', '   '",
            "'여러 공백', '    '"
    })
    @DisplayName("updateEvent 성공 - description 공백/빈 문자열 처리")
    void updateEvent_success_descriptionWhitespaceAndEmpty(String testDescription, String inputDescription) throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        Event existingEvent = createEvent(eventList, "Original Title", "Original Description");
        String accessToken = createJwtToken(user);

        String requestJson = createPartialUpdateEventRequestJson("description", inputDescription);

        // WHEN - PUT /events/{id} 호출
        mockMvc.perform(put("/events/{id}", existingEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 공백이 trim되어 빈 문자열로 저장
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.description").value(""));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'공백 문자열', '   '",
            "'빈 문자열', ''",
            "'여러 공백', '    '"
    })
    @DisplayName("updateEvent 성공 - title 공백/빈 문자열 처리 (값 변경 안됨)")
    void updateEvent_success_titleWhitespaceIgnored(String testDescription, String inputTitle) throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String originalTitle = "Original Title";
        Event existingEvent = createEvent(eventList, originalTitle, "Original Description");
        String accessToken = createJwtToken(user);

        String requestJson = createPartialUpdateEventRequestJson("title", inputTitle);

        // WHEN - PUT /events/{id} 호출
        mockMvc.perform(put("/events/{id}", existingEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 원본 title 값 유지 (변경되지 않음)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.title").value(originalTitle));
    }

    @Test
    @DisplayName("updateEvent 성공 - 모든 필드 null 요청 (값 변경 없음)")
    void updateEvent_success_allFieldsNull() throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        Task task = createTask(user);
        Event existingEvent = createEventWithTasks(eventList, "Original Title", List.of(task.getId()));
        String accessToken = createJwtToken(user);

        // 원본 값들 저장
        String originalTitle = existingEvent.getTitle();
        String originalDescription = existingEvent.getDescription();
        LocalDateTime originalStartDate = existingEvent.getStartDate();
        LocalDateTime originalEndDate = existingEvent.getEndDate();
        Boolean originalIsAllDay = existingEvent.getIsAllDay();
        Long originalEventListId = existingEvent.getEventList().getId();

        String requestJson = "{}"; // 모든 필드 null

        // WHEN - PUT /events/{id} 호출
        mockMvc.perform(put("/events/{id}", existingEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 모든 값이 원본 유지
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.eventListId").value(originalEventListId))
                .andExpect(jsonPath("$.event.title").value(originalTitle))
                .andExpect(jsonPath("$.event.description").value(originalDescription))
                .andExpect(jsonPath("$.event.eventDateTime.startDate").value(originalStartDate.format(DATE_TIME_FORMATTER)))
                .andExpect(jsonPath("$.event.eventDateTime.endDate").value(originalEndDate.format(DATE_TIME_FORMATTER)))
                .andExpect(jsonPath("$.event.eventDateTime.isAllDay").value(originalIsAllDay))
                .andExpect(jsonPath("$.event.taskIds.length()").value(1))
                .andExpect(jsonPath("$.event.taskIds[0]").value(task.getId()));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'종료날짜가 시작날짜보다 이전 - 날짜지정', '2024-01-02T10:00:00', '2024-01-01T10:00:00', false",
            "'종료날짜가 시작날짜보다 이전 - 종일일정', '2024-01-02T00:00:00', '2024-01-01T00:00:00', true",
            "'종일일정이지만 시작시간이 자정 아님', '2024-01-01T01:00:00', '2024-01-02T00:00:00', true",
            "'종일일정이지만 종료시간이 자정 아님', '2024-01-01T00:00:00', '2024-01-02T01:00:00', true",
            "'종일일정이지만 하루 미만 차이', '2024-01-01T00:00:00', '2024-01-01T00:00:00', true"
    })
    @DisplayName("updateEvent 실패 - 유효하지 않은 eventDateTime")
    void updateEvent_fail_invalidEventDateTime(String testDescription, String startDate, String endDate, Boolean isAllDay) throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        Event existingEvent = createEvent(eventList, "Original Title", "Original Description");
        String accessToken = createJwtToken(user);

        String requestJson = createEventRequestJson(
                eventList.getId(), "Updated Title",
                startDate, endDate, isAllDay, null, "Updated Description"
        );

        // WHEN & THEN
        mockMvc.perform(put("/events/{id}", existingEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("updateEvent 실패 - 존재하지 않는 eventId")
    void updateEvent_fail_eventNotFound() throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);
        Long nonExistentEventId = getNonExistentEventId();

        String requestJson = createEventRequestJson(
                eventList.getId(), "Updated Title",
                TEST_START_DATE.format(DATE_TIME_FORMATTER),
                TEST_END_DATE.format(DATE_TIME_FORMATTER),
                false, null, "Updated Description"
        );

        // WHEN & THEN
        mockMvc.perform(put("/events/{id}", nonExistentEventId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("일정이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("updateEvent 실패 - 다른 사용자의 Event")
    void updateEvent_fail_eventNotOwner() throws Exception {
        // GIVEN - 두 명의 사용자 생성
        User eventOwner = createTestUser();
        User otherUser = createTestUser();

        EventList ownerEventList = createEventList(eventOwner, DEFAULT_EVENTLIST_NAME);
        Event ownerEvent = createEvent(ownerEventList, "Owner's Event", "Owner's Description");

        EventList otherUserEventList = createEventList(otherUser, DEFAULT_EVENTLIST_NAME);
        String otherUserToken = createJwtToken(otherUser);

        String requestJson = createEventRequestJson(
                otherUserEventList.getId(), "Updated Title",
                TEST_START_DATE.format(DATE_TIME_FORMATTER),
                TEST_END_DATE.format(DATE_TIME_FORMATTER),
                false, null, "Updated Description"
        );

        // WHEN & THEN
        mockMvc.perform(put("/events/{id}", ownerEvent.getId())
                        .header("Authorization", "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("일정이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("updateEvent 실패 - 존재하지 않는 EventList")
    void updateEvent_fail_eventListNotFound() throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        Event existingEvent = createEvent(eventList, "Original Title", "Original Description");
        String accessToken = createJwtToken(user);
        Long nonExistentEventListId = getNonExistentEventListId();

        String requestJson = createEventRequestJson(
                nonExistentEventListId, "Updated Title",
                TEST_START_DATE.format(DATE_TIME_FORMATTER),
                TEST_END_DATE.format(DATE_TIME_FORMATTER),
                false, null, "Updated Description"
        );

        // WHEN & THEN
        mockMvc.perform(put("/events/{id}", existingEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_LIST_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("일정리스트가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("updateEvent 실패 - 다른 사용자의 EventList")
    void updateEvent_fail_eventListNotOwner() throws Exception {
        // GIVEN - 두 명의 사용자 생성
        User eventOwner = createTestUser();
        User otherUser = createTestUser();

        EventList ownerEventList = createEventList(eventOwner, DEFAULT_EVENTLIST_NAME);
        Event ownerEvent = createEvent(ownerEventList, "Owner's Event", "Owner's Description");

        EventList otherUserEventList = createEventList(otherUser, DEFAULT_EVENTLIST_NAME);
        String ownerToken = createJwtToken(eventOwner);

        String requestJson = createEventRequestJson(
                otherUserEventList.getId(), "Updated Title", // 다른 사용자의 EventList 사용
                TEST_START_DATE.format(DATE_TIME_FORMATTER),
                TEST_END_DATE.format(DATE_TIME_FORMATTER),
                false, null, "Updated Description"
        );

        // WHEN & THEN
        mockMvc.perform(put("/events/{id}", ownerEvent.getId())
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_LIST_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("일정리스트가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("updateEvent 실패 - 존재하지 않는 Task")
    void updateEvent_fail_taskNotFound() throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        Event existingEvent = createEvent(eventList, "Original Title", "Original Description");
        String accessToken = createJwtToken(user);
        Long nonExistentTaskId = getNonExistentTaskId();

        String requestJson = createEventRequestJson(
                eventList.getId(), "Updated Title",
                TEST_START_DATE.format(DATE_TIME_FORMATTER),
                TEST_END_DATE.format(DATE_TIME_FORMATTER),
                false, List.of(nonExistentTaskId), "Updated Description"
        );

        // WHEN & THEN
        mockMvc.perform(put("/events/{id}", existingEvent.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("할일이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("updateEvent 실패 - 다른 사용자의 Task")
    void updateEvent_fail_taskNotOwner() throws Exception {
        // GIVEN - 두 명의 사용자 생성
        User eventOwner = createTestUser();
        User otherUser = createTestUser();

        EventList ownerEventList = createEventList(eventOwner, DEFAULT_EVENTLIST_NAME);
        Event ownerEvent = createEvent(ownerEventList, "Owner's Event", "Owner's Description");
        Task otherUserTask = createTask(otherUser);
        String ownerToken = createJwtToken(eventOwner);

        String requestJson = createEventRequestJson(
                ownerEventList.getId(), "Updated Title",
                TEST_START_DATE.format(DATE_TIME_FORMATTER),
                TEST_END_DATE.format(DATE_TIME_FORMATTER),
                false, List.of(otherUserTask.getId()), "Updated Description" // 다른 사용자의 Task 사용
        );

        // WHEN & THEN
        mockMvc.perform(put("/events/{id}", ownerEvent.getId())
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("할일이 존재하지 않습니다."));
    }

    // 헬퍼 메서드
    private Event createEventWithDates(EventList eventList, String title, LocalDateTime startDate, LocalDateTime endDate) {
        Event event = Event.builder()
                .eventList(eventList)
                .title(title)
                .startDate(startDate)
                .endDate(endDate)
                .isAllDay(false)
                .description(DEFAULT_DESCRIPTION)
                .build();
        return eventRepository.save(event);
    }

    private Event createEventWithTasks(EventList eventList, String title, List<Long> taskIds) {
        Event event = Event.builder()
                .eventList(eventList)
                .title(title)
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .isAllDay(false)
                .description(DEFAULT_DESCRIPTION)
                .build();

        Event savedEvent = eventRepository.save(event);

        // EventTask 연결 생성 (실제 서비스 로직처럼)
        for (Long taskId : taskIds) {
            Task task = taskRepository.findById(taskId).orElseThrow();
            EventTask eventTask = EventTask.builder()
                    .event(savedEvent)
                    .task(task)
                    .build();
            savedEvent.getEventTasks().add(eventTask);
        }

        return eventRepository.save(savedEvent);
    }

    // 부분 업데이트용 헬퍼 메서드
    private String createPartialUpdateEventRequestJson(String fieldName, String fieldValue) {
        if ("title".equals(fieldName)) {
            return createEventRequestJson(null, fieldValue, null, null, null, null, null);
        } else if ("description".equals(fieldName)) {
            return createEventRequestJson(null, null, null, null, null, null, fieldValue);
        }
        return "{}";
    }

    // ================ deleteEvent 테스트 ================

    @Test
    @DisplayName("일정을 삭제를 성공하면 성공 응답을 반환한다.")
    void deleteEvent_success() throws Exception {
        // GIVEN - 사용자와 이벤트 생성
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        Event event = createEvent(eventList, DEFAULT_EVENT_TITLE, DEFAULT_DESCRIPTION);
        String accessToken = createJwtToken(user);

        // WHEN - DELETE /events/{id} 호출
        mockMvc.perform(delete("/events/{id}", event.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                // THEN - 성공 응답 검증
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("일정이 삭제되었습니다."));
    }

    @Test
    @DisplayName("일정을 삭제 성공 후 조회하면 NOT_FOUND 응답을 반환한다.")
    void deleteEvent_success_eventNotFoundAfterDeletion() throws Exception {
        // GIVEN - 사용자와 이벤트 생성
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        Event event = createEvent(eventList, DEFAULT_EVENT_TITLE, DEFAULT_DESCRIPTION);
        String accessToken = createJwtToken(user);

        // WHEN - 이벤트 삭제
        mockMvc.perform(delete("/events/{id}", event.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // THEN - 삭제된 이벤트 조회 시 NOT_FOUND 응답
        mockMvc.perform(get("/events/{id}", event.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("일정이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("일정 삭제 성공 후 캘린더뷰로 조회하면 해당 일정이 조회되지 않는다.")
    void deleteEvent_success_notVisibleInCalendarAfterDeletion() throws Exception {
        // GIVEN - 사용자와 이벤트 생성
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        Event event = createEventWithDates(eventList, DEFAULT_EVENT_TITLE,
                LocalDateTime.of(2024, 4, 15, 10, 0),
                LocalDateTime.of(2024, 4, 15, 12, 0));
        String accessToken = createJwtToken(user);

        // 삭제 전 캘린더 조회로 이벤트 존재 확인
        mockMvc.perform(get("/events")
                        .param("from", DEFAULT_FROM_DATE)
                        .param("to", DEFAULT_TO_DATE)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // WHEN - 이벤트 삭제
        mockMvc.perform(delete("/events/{id}", event.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // THEN - 캘린더 조회 시 이벤트가 조회되지 않음
        mockMvc.perform(get("/events")
                        .param("from", DEFAULT_FROM_DATE)
                        .param("to", DEFAULT_TO_DATE)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("일정을 성공적으로 삭제하면 해당 일정만 삭제되고 나머지는 존재해야 한다.")
    void deleteEvent_success_onlyTargetEventDeleted() throws Exception {
        // GIVEN - 사용자와 여러 이벤트 생성
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        Event event1 = createEventWithDates(eventList, "Event 1",
                LocalDateTime.of(2024, 4, 10, 10, 0),
                LocalDateTime.of(2024, 4, 10, 12, 0));
        Event event2 = createEventWithDates(eventList, "Event 2",
                LocalDateTime.of(2024, 4, 15, 10, 0),
                LocalDateTime.of(2024, 4, 15, 12, 0));
        Event event3 = createEventWithDates(eventList, "Event 3",
                LocalDateTime.of(2024, 4, 20, 10, 0),
                LocalDateTime.of(2024, 4, 20, 12, 0));
        String accessToken = createJwtToken(user);

        // 삭제 전 이벤트 3개 존재 확인
        mockMvc.perform(get("/events")
                        .param("from", DEFAULT_FROM_DATE)
                        .param("to", DEFAULT_TO_DATE)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        // WHEN - 중간 이벤트(event2) 삭제
        mockMvc.perform(delete("/events/{id}", event2.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // THEN - 나머지 2개 이벤트는 여전히 존재
        mockMvc.perform(get("/events")
                        .param("from", DEFAULT_FROM_DATE)
                        .param("to", DEFAULT_TO_DATE)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].eventId").value(event1.getId()))
                .andExpect(jsonPath("$[0].title").value("Event 1"))
                .andExpect(jsonPath("$[1].eventId").value(event3.getId()))
                .andExpect(jsonPath("$[1].title").value("Event 3"));
    }

    @Test
    @DisplayName("일정을 성공적으로 삭제하면 연관된 eventTask도 함께 삭제된다.(Task는 존재해야 함)")
    void deleteEvent_success_cascadesEventTaskDeletion() throws Exception {
        // GIVEN - 사용자, 태스크가 연결된 이벤트 생성
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        Task task1 = createTask(user);
        Task task2 = createTask(user);
        Event event = createEventWithTasks(eventList, DEFAULT_EVENT_TITLE, List.of(task1.getId(), task2.getId()));
        String accessToken = createJwtToken(user);

        // EventTask 연결 확인
        mockMvc.perform(get("/events/{id}", event.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskIds.length()").value(2));

        // WHEN - 이벤트 삭제
        mockMvc.perform(delete("/events/{id}", event.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        // THEN - 이벤트 삭제로 EventTask도 삭제되어 조회 불가
        mockMvc.perform(get("/events/{id}", event.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // TODO) Task는 여전히 존재해야 함 (Task API 개발 후 검증 추가)
    }

    @Test
    @DisplayName("존재하지 않는 일정으로 삭제 요청하면 NOT_FOUND 응답을 반환한다.")
    void deleteEvent_fail_nonExistentEvent() throws Exception {
        // GIVEN - 사용자 생성
        User user = createTestUser();
        String accessToken = createJwtToken(user);
        Long nonExistentEventId = getNonExistentEventId();

        // WHEN & THEN - 존재하지 않는 이벤트 삭제 시 NOT_FOUND
        mockMvc.perform(delete("/events/{id}", nonExistentEventId)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("일정이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("소유자가 다른 일정을 삭제 요청하면 ACCESS_DENIED 예외가 발생하지만 NOT_FOUND 응답을 반환한다.")
    void deleteEvent_fail_unauthorizedUser() throws Exception {
        // GIVEN - 두 명의 사용자 생성
        User eventOwner = createTestUser();
        User otherUser = createTestUser();

        EventList ownerEventList = createEventList(eventOwner, DEFAULT_EVENTLIST_NAME);
        Event ownerEvent = createEvent(ownerEventList, DEFAULT_EVENT_TITLE, DEFAULT_DESCRIPTION);
        String otherUserToken = createJwtToken(otherUser);

        // WHEN & THEN - 다른 사용자가 이벤트 삭제 시도 시 NOT_FOUND (보안상 존재 여부 노출 안함)
        mockMvc.perform(delete("/events/{id}", ownerEvent.getId())
                        .header("Authorization", "Bearer " + otherUserToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("일정이 존재하지 않습니다."));
    }
}
