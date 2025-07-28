package org.mi.plannitybe.schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
@Entity
@Table(name = "event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
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
    private LocalDateTime startDate;

    @Column(length = 255)
    @Comment("종료 날짜")
    private LocalDateTime endDate;

    @Column(nullable = false)
    @Comment("종일일정여부")
    private Boolean isAllDay;

    @Column(length = 255)
    @Comment("설명")
    private String description;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EventTask> eventTasks = new ArrayList<>();

    public void addEventTask(EventTask eventTask) {
        eventTasks.add(eventTask);
    }

    public void update(String title, LocalDateTime startDate, LocalDateTime endDate,
                       Boolean isAllDay, String description) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isAllDay = isAllDay;
        this.description = description;
    }
}