package org.mi.plannitybe.schedule.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mi.plannitybe.integration.UserSetUp;
import org.mi.plannitybe.schedule.entity.EventList;
import org.mi.plannitybe.user.entity.User;
import org.mi.plannitybe.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

//@DataJpaTest
@SpringBootTest
@Transactional
class EventListRepositoryTest {

    @Autowired
    private EventListRepository eventListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSetUp userSetUp;

    @Test
    @DisplayName("eventListId로 해당 eventList의 소유자 userId 조회")
    void findUserIdByEventListIdTest() {
        // GIVEN - user와 해당 userId가 저장된 eventList
        String userId = userSetUp.saveUser("test@eamil.com", "pwd123!");
        User user = userRepository.findById(userId).orElseThrow();
        EventList eventList = EventList.builder()
                .user(user)
                .name("test")
                .isDefault(false)
                .build();
        Long eventListId = eventListRepository.save(eventList).getId();

        // WHEN - 저장한 eventListId로 eventList의 userId 조회
        String userIdByEventListId = eventListRepository.findUserIdByEventListId(eventListId);

        // THEN - 조회한 userId가 처음 저장한 userId와 같아야 한다.
        assertEquals(userId, userIdByEventListId);
    }
}