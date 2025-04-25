package org.mi.plannitybe.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;
import org.mi.plannitybe.policy.entity.Term;

@Entity
@Table(name = "user_term")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(UserTermId.class)
public class UserTerm extends BaseEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    @Comment("약관 ID")
    private Term term;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Comment("사용자 ID")
    private User user;

    @Column(nullable = false)
    @Comment("동의 여부")
    private Boolean agreed;

    @Column(length = 255)
    @Comment("동의 일시")
    private String agreedAt;

    @Builder
    public UserTerm(Term term, User user, Boolean agreed, String agreedAt) {
        this.term = term;
        this.user = user;
        this.agreed = agreed;
        this.agreedAt = agreedAt;
    }

    public void update(Boolean agreed, String agreedAt) {
        this.agreed = agreed;
        this.agreedAt = agreedAt;
    }
}