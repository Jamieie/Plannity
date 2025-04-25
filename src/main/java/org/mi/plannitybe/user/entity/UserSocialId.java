package org.mi.plannitybe.user.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.mi.plannitybe.user.type.SocialProviderType;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserSocialId implements Serializable {
    private String user; // User 엔티티의 ID 필드는 String
    private SocialProviderType provider; // enum 타입으로 변경
}