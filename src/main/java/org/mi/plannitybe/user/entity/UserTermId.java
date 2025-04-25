package org.mi.plannitybe.user.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserTermId implements Serializable {
    private Long term; // JPA에서 연관된 엔티티의 ID 필드 타입을 사용
    private String user; // User 엔티티의 ID 필드는 String
}