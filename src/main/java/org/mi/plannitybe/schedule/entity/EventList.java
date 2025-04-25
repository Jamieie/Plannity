package org.mi.plannitybe.schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;
import org.mi.plannitybe.user.entity.User;

@Entity
@Table(name = "event_list")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("이벤트 목록 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Comment("사용자 ID")
    private User user;

    @Column(length = 255)
    @Comment("이벤트 목록 이름")
    private String name;

    @Column(length = 255)
    @Comment("색상")
    private String color;

    @Column(nullable = false)
    @Comment("기본 목록 여부")
    private Boolean defaultList;

    @Builder
    public EventList(User user, String name, String color, Boolean defaultList) {
        this.user = user;
        this.name = name;
        this.color = color;
        this.defaultList = defaultList;
    }

    public void update(String name, String color, Boolean defaultList) {
        this.name = name;
        this.color = color;
        this.defaultList = defaultList;
    }
}