package org.mi.plannitybe.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;

@Entity
@Table(name = "common_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("공통 코드 ID")
    private Long id;

    @Column(nullable = false, length = 50)
    @Comment("코드 그룹")
    private String codeGroup;

    @Column(nullable = false, length = 50)
    @Comment("코드")
    private String code;

    @Column(nullable = false, length = 100)
    @Comment("코드명")
    private String codeName;

    @Column(length = 500)
    @Comment("코드 설명")
    private String description;

    @Column(nullable = false)
    @Comment("사용 여부")
    private Boolean useYn;

    @Column(nullable = false)
    @Comment("정렬 순서")
    private Integer sortOrder;

    @Builder
    public CommonCode(String codeGroup, String code, String codeName, String description, Boolean useYn, Integer sortOrder) {
        this.codeGroup = codeGroup;
        this.code = code;
        this.codeName = codeName;
        this.description = description;
        this.useYn = useYn;
        this.sortOrder = sortOrder;
    }

    /**
     * 공통 코드 수정
     */
    public void update(String codeName, String description, Boolean useYn, Integer sortOrder) {
        this.codeName = codeName;
        this.description = description;
        this.useYn = useYn;
        this.sortOrder = sortOrder;
    }
}