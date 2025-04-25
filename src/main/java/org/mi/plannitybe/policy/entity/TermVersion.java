package org.mi.plannitybe.policy.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "term_version")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(TermVersionId.class)
public class TermVersion extends BaseEntity {

    @Id
    @Column(length = 255)
    @Comment("약관 버전")
    private String version;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    @Comment("약관 ID")
    private Term term;

    @Column(length = 255)
    @Comment("약관 이름")
    private String termName;

    @Column(length = 255)
    @Comment("약관 내용")
    private String termContent;

    @Column(length = 255)
    @Comment("공지 일시")
    private String announcedAt;

    @Column(length = 255)
    @Comment("시작 일자")
    private String startDate;

    @Column(length = 255)
    @Comment("종료 일자")
    private String endDate;

    @Builder
    public TermVersion(String version, Term term, String termName, String termContent, 
                      String announcedAt, String startDate, String endDate) {
        this.version = version;
        this.term = term;
        this.termName = termName;
        this.termContent = termContent;
        this.announcedAt = announcedAt;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void update(String termName, String termContent, String announcedAt, 
                      String startDate, String endDate) {
        this.termName = termName;
        this.termContent = termContent;
        this.announcedAt = announcedAt;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}