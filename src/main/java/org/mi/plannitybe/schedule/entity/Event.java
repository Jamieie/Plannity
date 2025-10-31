package org.mi.plannitybe.schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;
import org.mi.plannitybe.schedule.domain.EventDateTime;

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

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<EventTask> eventTasks = new ArrayList<>();

    public void addTask(Task task) {
        if (hasTask(task)) {
            return;
        }

        EventTask eventTask = EventTask.builder()
                .event(this)
                .task(task)
                .build();
        this.eventTasks.add(eventTask);
    }

    private boolean hasTask(Task task) {
        return this.eventTasks.stream()
                .anyMatch(eventTask -> Objects.equals(eventTask.getTask().getId(), task.getId()));
    }

    public void removeTask(Task task) {
        this.eventTasks.stream()
                .filter(eventTask -> Objects.equals(eventTask.getTask().getId(), task.getId()))
                .findFirst()
                .ifPresent(eventTask -> {
                    this.eventTasks.remove(eventTask);
                    eventTask.setEvent(null);  // 양방향 연관관계 동기화
                });
    }

    public EventDateTime getEventDateTime() {
        return EventDateTime.of(startDate, endDate, isAllDay);
    }

    public void updateEventList(EventList eventList) {
        if (eventList != null) {
            this.eventList = eventList;
        }
    }

    public void updateTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title;
        }
    }

    public void updateFromEventDateTime(EventDateTime eventDateTime) {
        if (eventDateTime != null) {
            this.startDate = eventDateTime.getStartDate();
            this.endDate = eventDateTime.getEndDate();
            this.isAllDay = eventDateTime.getIsAllDay();
        }
    }

    public void updateDescription(String description) {
        if (description != null) {
            this.description = description;
        }
    }
}