package org.mi.plannitybe.schedule.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mi.plannitybe.integration.BaseIntegrationTest;
import org.mi.plannitybe.integration.UserSetUp;
import org.mi.plannitybe.jwt.JwtTokenProvider;
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
import org.mi.plannitybe.schedule.type.TaskStatusType;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
            "'날짜 없음, 종일일정 false', , , false, false",
            "'날짜 같음, 종일일정 false', '2024-01-01T10:00:00', '2024-01-01T10:00:00', false, false",
            "'시작날짜 < 종료날짜, 종일일정 false', '2024-01-01T10:00:00', '2024-01-01T12:00:00', false, false",
            "'종일일정 true, 자정, 하루 차이', '2024-01-01T00:00:00', '2024-01-02T00:00:00', true, false",
            "'종일일정 true, 자정, 여러 날 차이', '2024-01-01T00:00:00', '2024-01-05T00:00:00', true, false",
            "'날짜 없음, Tasks 있음', , , false, true",
            "'날짜 있음, Tasks 있음', '2024-01-01T10:00:00', '2024-01-01T12:00:00', false, true"
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
                .andExpect(jsonPath("$.event.isAllDay").value(isAllDay));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'한쪽 날짜만 존재 - startDate만', '2024-01-01T10:00:00', , false",
            "'한쪽 날짜만 존재 - endDate만', , '2024-01-01T10:00:00', false",
            "'종료날짜가 시작날짜보다 이전', '2024-01-02T10:00:00', '2024-01-01T10:00:00', false"
    })
    @DisplayName("createEvent 실패 - 잘못된 날짜 조합")
    void createEvent_fail_invalidDates(String testDescription, LocalDateTime startDate,
                                       LocalDateTime endDate, Boolean isAllDay) throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);

        String requestJson = createEventRequestJson(
                eventList.getId(), DEFAULT_EVENT_TITLE,
                startDate != null ? startDate.format(DATE_TIME_FORMATTER) : null,
                endDate != null ? endDate.format(DATE_TIME_FORMATTER) : null,
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
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("validDateCombination")));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'종일일정이지만 날짜 없음', , , true",
            "'종일일정이지만 하루 미만 차이', '2024-01-01T00:00:00', '2024-01-01T00:00:00', true",
            "'종일일정이지만 시작 시간이 자정 아님', '2024-01-01T01:00:00', '2024-01-02T00:00:00', true",
            "'종일일정이지만 종료 시간이 자정 아님', '2024-01-01T00:00:00', '2024-01-02T01:00:00', true"
    })
    @DisplayName("createEvent 실패 - 종일일정 날짜 검증")
    void createEvent_fail_allDayDateValidation(String testDescription, LocalDateTime startDate,
                                               LocalDateTime endDate, Boolean isAllDay) throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);

        String requestJson = createEventRequestJson(
                eventList.getId(), DEFAULT_EVENT_TITLE,
                startDate != null ? startDate.format(DATE_TIME_FORMATTER) : null,
                endDate != null ? endDate.format(DATE_TIME_FORMATTER) : null,
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
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("createEvent 실패 - 존재하지 않는 eventListId")
    void createEvent_fail_eventListNotFound() throws Exception {
        // GIVEN
        User user = createTestUser();
        String accessToken = createJwtToken(user);
        Long nonExistentEventListId = 99999L;

        String requestJson = createEventRequestJson(
                nonExistentEventListId, DEFAULT_EVENT_TITLE, null, null, false, null
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
                ownerEventList.getId(), DEFAULT_EVENT_TITLE, null, null, false, null
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
            "'eventListId null', , 'Test Event', false, 'eventListId'",
            "'title null', '1', , false, 'title'",
            "'title 빈 문자열', '1', '', false, 'title'",
            "'title 공백만', '1', '   ', false, 'title'",
            "'isAllDay null', '1', 'Test Event', , 'isAllDay'"
    })
    @DisplayName("createEvent 실패 - 필수 필드 누락")
    void createEvent_fail_requiredFields(String testDescription, String eventListId,
                                         String title, String isAllDay, String expectedField) throws Exception {
        // GIVEN
        User user = createTestUser();
        EventList eventList = createEventList(user, DEFAULT_EVENTLIST_NAME);
        String accessToken = createJwtToken(user);

        // eventListId가 null이 아니면 실제 eventList ID 사용
        Long actualEventListId = eventListId != null ? eventList.getId() : null;
        Boolean actualIsAllDay = isAllDay != null ? Boolean.valueOf(isAllDay) : null;

        System.out.println("=== eventListId : " + eventListId);
        System.out.println("=== title : " + title);

        String requestJson = createEventRequestJson(
                actualEventListId, title, null, null, actualIsAllDay, null
        );
        System.out.println("=== json : " + requestJson);

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

        List<Long> nonExistentTaskIds = List.of(99999L);
        String requestJson = createEventRequestJson(
                eventList.getId(), DEFAULT_EVENT_TITLE, null, null, false, nonExistentTaskIds
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
                eventList.getId(), DEFAULT_EVENT_TITLE, null, null, false, otherUserTaskIds
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
                eventList.getId(), DEFAULT_EVENT_TITLE, null, null, false, null
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
            "{\"eventListId\":1,\"title\":\"Test\",\"isAllDay\":false,\"startDate\":\"invalid-date\"}",
            "{\"eventListId\":1,\"title\":\"Test\",\"isAllDay\":false,\"startDate\":\"2024-13-01T00:00:00\"}",
            "{\"eventListId\":1,\"title\":\"Test\",\"isAllDay\":false,\"startDate\":\"2024/01/01 10:00:00\"}",
            "{\"eventListId\":\"invalid-id\",\"title\":\"Test\",\"isAllDay\":false}",
            "{\"eventListId\":1,\"title\":\"Test\",\"isAllDay\":\"invalid-allDay\"}",
            "{\"eventListId\":1,\"title\":\"Test\",\"isAllDay:false}",
            "{\"eventListId\":1,\"title\":\"Test\",\"isAllDay\"false}",
            "{\"eventListId\":1,\"title\":\"Test\",isAllDay:false}",
            "{\"eventListId\":1,\"title\":\"Test\"\"isAllDay\":false}"
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
                .andExpect(jsonPath("$.startDate").value(savedEvent.getStartDate().format(DATE_TIME_FORMATTER)))
                .andExpect(jsonPath("$.endDate").value(savedEvent.getEndDate().format(DATE_TIME_FORMATTER)))
                .andExpect(jsonPath("$.isAllDay").value(savedEvent.getIsAllDay()))
                .andExpect(jsonPath("$.description").value(savedEvent.getDescription()))
                .andExpect(jsonPath("$.eventTaskIds").isArray());
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
    @ValueSource(strings = {"invalidEventId", "one", "-", "9223372036854775808"})  // 9223372036854775808 = Long의 최대값 + 1
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


    private String createEventRequestJson(Long eventListId, String title,
                                          String startDate, String endDate,
                                          Boolean isAllDay, List<Long> taskIds) {
        StringBuilder json = new StringBuilder("{");

        if (eventListId != null) {
            json.append("\"eventListId\":").append(eventListId).append(",");
        }
        if (title != null) {
            json.append("\"title\":\"").append(title).append("\",");
        }
        if (startDate != null) {
            json.append("\"startDate\":\"").append(startDate).append("\",");
        }
        if (endDate != null) {
            json.append("\"endDate\":\"").append(endDate).append("\",");
        }
        if (isAllDay != null) {
            json.append("\"isAllDay\":").append(isAllDay).append(",");
        }
        json.append("\"description\":\"").append(DEFAULT_DESCRIPTION).append("\"");

        if (taskIds != null && !taskIds.isEmpty()) {
            json.append(",\"tasks\":[");
            json.append(taskIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(""));
            json.append("]");
        }

        json.append("}");
        return json.toString();
    }
}
