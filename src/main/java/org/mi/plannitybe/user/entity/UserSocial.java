package org.mi.plannitybe.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;
import org.mi.plannitybe.user.type.SocialProviderType;

@Entity
@Table(name = "user_social")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(UserSocialId.class)
public class UserSocial extends BaseEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Comment("사용자 ID")
    private User user;

    @Id
    @Column(length = 20)
    @Comment("소셜 제공자")
    private SocialProviderType provider;

    @Column(length = 255)
    @Comment("제공자 ID")
    private String providerId;

    @Column(nullable = false)
    @Comment("연결 여부")
    private Boolean connected;

    @Builder
    public UserSocial(User user, SocialProviderType provider, String providerId, Boolean connected) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
        this.connected = connected;
    }

    public void update(String providerId, Boolean connected) {
        this.providerId = providerId;
        this.connected = connected;
    }
}