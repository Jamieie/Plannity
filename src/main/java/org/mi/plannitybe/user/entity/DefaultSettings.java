package org.mi.plannitybe.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;
import org.mi.plannitybe.user.type.DateFormatType;
import org.mi.plannitybe.user.type.TimeFormatType;
import org.mi.plannitybe.user.type.WeekStartType;

@Entity
@Table(name = "default_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DefaultSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("설정 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Comment("사용자 ID")
    private User user;

    @Column(length = 20)
    @Comment("날짜 형식")
    private DateFormatType dateFormat;

    @Column(length = 20)
    @Comment("시간 형식")
    private TimeFormatType timeFormat;

    @Column(length = 50)
    @Comment("시간대")
    private String timeZone;

    @Column(length = 255)
    @Comment("이벤트 기본 기간")
    private String eventDefaultDuration;

    @Column(length = 20)
    @Comment("주 시작일")
    private WeekStartType startWeekOn;

    @Column(nullable = false)
    @Comment("사용자 정의 보기 설정")
    private Boolean customView;

    @Column(nullable = false)
    @Comment("주 번호 표시 여부")
    private Boolean showWeekNumbers;

    @Builder
    public DefaultSettings(User user, DateFormatType dateFormat, TimeFormatType timeFormat, 
                          String timeZone, String eventDefaultDuration, 
                          WeekStartType startWeekOn, Boolean customView, Boolean showWeekNumbers) {
        this.user = user;
        this.dateFormat = dateFormat;
        this.timeFormat = timeFormat;
        this.timeZone = timeZone;
        this.eventDefaultDuration = eventDefaultDuration;
        this.startWeekOn = startWeekOn;
        this.customView = customView;
        this.showWeekNumbers = showWeekNumbers;
    }

    public void update(DateFormatType dateFormat, TimeFormatType timeFormat, String timeZone,
                      String eventDefaultDuration, WeekStartType startWeekOn,
                      Boolean customView, Boolean showWeekNumbers) {
        this.dateFormat = dateFormat;
        this.timeFormat = timeFormat;
        this.timeZone = timeZone;
        this.eventDefaultDuration = eventDefaultDuration;
        this.startWeekOn = startWeekOn;
        this.customView = customView;
        this.showWeekNumbers = showWeekNumbers;
    }
}