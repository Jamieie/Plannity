package org.mi.plannitybe.schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;
import org.mi.plannitybe.schedule.type.TaskStatusType;

@Entity
@Table(name = "task")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("작업 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_list_id")
    @Comment("작업 목록 ID")
    private TaskList taskList;

    @Column(length = 255)
    @Comment("제목")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_task_id")
    @Comment("상위 작업 ID")
    private Task mainTask;

    @Column(length = 255)
    @Comment("예상 소요 시간")
    private String estimatedDuration;

    @Column(length = 255)
    @Comment("실제 소요 시간")
    private String actualDuration;

    @Column(length = 20)
    @Comment("상태")
    private TaskStatusType status;

    @Column(length = 255)
    @Comment("알림 날짜")
    private String reminderDate;

    @Column(length = 255)
    @Comment("알림 시간")
    private String reminderTime;

    @Column(length = 255)
    @Comment("설명")
    private String description;

    @Builder
    public Task(TaskList taskList, String title, Task mainTask, String estimatedDuration,
               String actualDuration, TaskStatusType status, String reminderDate,
               String reminderTime, String description) {
        this.taskList = taskList;
        this.title = title;
        this.mainTask = mainTask;
        this.estimatedDuration = estimatedDuration;
        this.actualDuration = actualDuration;
        this.status = status;
        this.reminderDate = reminderDate;
        this.reminderTime = reminderTime;
        this.description = description;
    }

    public void update(String title, Task mainTask, String estimatedDuration,
                      String actualDuration, TaskStatusType status, String reminderDate,
                      String reminderTime, String description) {
        this.title = title;
        this.mainTask = mainTask;
        this.estimatedDuration = estimatedDuration;
        this.actualDuration = actualDuration;
        this.status = status;
        this.reminderDate = reminderDate;
        this.reminderTime = reminderTime;
        this.description = description;
    }
}