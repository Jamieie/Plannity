package org.mi.plannitybe.schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;

@Entity
@Table(name = "event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("이벤트 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_list_id")
    @Comment("이벤트 목록 ID")
    private EventList eventList;

    @Column(nullable = false, length = 255)
    @Comment("제목")
    private String title;

    @Column(length = 255)
    @Comment("시작 날짜")
    private String startDate;

    @Column(length = 255)
    @Comment("종료 날짜")
    private String endDate;

    @Column(length = 255)
    @Comment("시작 시간")
    private String startTime;

    @Column(length = 255)
    @Comment("종료 시간")
    private String endTime;

    @Column(length = 255)
    @Comment("설명")
    private String description;

    @Builder
    public Event(EventList eventList, String title, String startDate, String endDate,
                String startTime, String endTime, String description) {
        this.eventList = eventList;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    public void update(String title, String startDate, String endDate,
                      String startTime, String endTime, String description) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }
}