package org.mi.plannitybe.policy.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;

@Entity
@Table(name = "term")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("약관 ID")
    private Long id;

    @Column(length = 255)
    @Comment("약관 이름")
    private String name;

    @Column(nullable = false)
    @Comment("필수 동의 여부")
    private Boolean required;

    // FIXME) TermType 필드 추가하기

    @Builder
    public Term(String name, Boolean required) {
        this.name = name;
        this.required = required;
    }

    public void update(String name, Boolean required) {
        this.name = name;
        this.required = required;
    }
}