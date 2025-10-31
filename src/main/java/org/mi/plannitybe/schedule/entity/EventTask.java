package org.mi.plannitybe.schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;

@Entity
@Table(name = "event_task")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("이벤트 작업 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @Comment("이벤트 ID")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    @Comment("작업 ID")
    private Task task;

    @Builder
    public EventTask(Event event, Task task) {
        this.event = event;
        this.task = task;
    }
}