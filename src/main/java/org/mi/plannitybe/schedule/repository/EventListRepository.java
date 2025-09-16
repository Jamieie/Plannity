package org.mi.plannitybe.schedule.repository;

import org.mi.plannitybe.schedule.entity.EventList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventListRepository extends JpaRepository<EventList, Long> {

    // eventListId로 eventList의 소유자 userId 조회
    @Query("SELECT eventList.user.id " +
            "FROM EventList eventList " +
            "WHERE eventList.id = :eventListId")
    String findUserIdByEventListId(long eventListId);

    /*
    <JPQL 실행구문>
    select el1_0.user_id
    from event_list el1_0
    where el1_0.id=?

    -> eventList.user.id라고 지정하면 user 객체를 다 불러오지 않고 sql 수준에서 user_id 컬럼 하나만 조회
    */

    List<EventList> findByUserId(String userId);
}
