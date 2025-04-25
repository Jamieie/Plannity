package org.mi.plannitybe.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;

@Entity
@Table(name = "user_term_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTermHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("이력 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "term_id", referencedColumnName = "term_id"),
        @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    })
    @Comment("사용자 약관")
    private UserTerm userTerm;

    @Column(nullable = false)
    @Comment("동의 여부")
    private Boolean agreed;

    @Column(length = 255)
    @Comment("동의 일시")
    private String agreedAt;

    @Column(length = 255)
    @Comment("시작 일자")
    private String startDate;

    @Column(length = 255)
    @Comment("종료 일자")
    private String endDate;

    @Builder
    public UserTermHistory(UserTerm userTerm, Boolean agreed, String agreedAt, 
                          String startDate, String endDate) {
        this.userTerm = userTerm;
        this.agreed = agreed;
        this.agreedAt = agreedAt;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}