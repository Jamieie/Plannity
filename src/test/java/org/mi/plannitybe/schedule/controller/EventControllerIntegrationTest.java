package org.mi.plannitybe.schedule.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


class EventControllerIntegrationTest extends BaseIntegrationTest {

    // 테스트 상수
    private static final String DEFAULT_PASSWORD = "password123";
    private static final String DEFAULT_COLOR = "#FF0000";
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
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("getEvent 성공 - 존재하는 이벤트 ID와 올바른 소유자로 조회")
    public void getEvent_success() throws Exception {
        // GIVEN - 사용자와 이벤트 데이터 생성
        User user = createTestUser(UUID.randomUUID().toString());
        EventList savedEventList = createEventList(user, "Test EventList");
        Event savedEvent = createEvent(savedEventList, "Test Event", "Test Description");
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
        User user = createTestUser(UUID.randomUUID().toString());
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
        User user = createTestUser(UUID.randomUUID().toString());
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
        User user = createTestUser(UUID.randomUUID().toString());
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
        User owner = createTestUser("owner" + UUID.randomUUID());
        EventList savedEventList = createEventList(owner, "Owner's EventList");
        Event savedEvent = createEvent(savedEventList, "Owner's Event", "Owner's Description");
        
        // GIVEN - 두 번째 사용자 생성 (이벤트의 소유자가 아님)
        User otherUser = createTestUser("other" + UUID.randomUUID());
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
    private User createTestUser(String emailPrefix) {
        String email = emailPrefix + "@test.com";
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

    // TODO) createEvent 통합 테스트 코드 작성
}