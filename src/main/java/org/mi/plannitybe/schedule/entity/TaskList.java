package org.mi.plannitybe.schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;
import org.mi.plannitybe.user.entity.User;

@Entity
@Table(name = "task_list")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("작업 목록 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Comment("사용자 ID")
    private User user;

    @Column(length = 255)
    @Comment("작업 목록 이름")
    private String name;

    @Column(length = 255)
    @Comment("색상")
    private String color;

    @Column(nullable = false)
    @Comment("기본 목록 여부")
    private Boolean isDefault;

    @Builder
    public TaskList(User user, String name, String color, Boolean isDefault) {
        this.user = user;
        this.name = name;
        this.color = color;
        this.isDefault = isDefault;
    }

    public void update(String name, String color, Boolean isDefault) {
        this.name = name;
        this.color = color;
        this.isDefault = isDefault;
    }
}