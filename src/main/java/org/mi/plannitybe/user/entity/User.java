package org.mi.plannitybe.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.mi.plannitybe.common.entity.base.BaseEntity;
import org.mi.plannitybe.user.type.UserRoleType;
import org.mi.plannitybe.user.type.UserStatusType;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @Column(length = 255)
    @Comment("사용자 ID")
    private String id;

    @Column(length = 255)
    @Comment("이메일")
    private String email;

    @Column(length = 255)
    @Comment("비밀번호")
    private String pwd;

    @Column(length = 255)
    @Comment("닉네임")
    private String nickname;

    @Column(length = 255)
    @Comment("전화번호")
    private String phoneNumber;

    @Column(length = 20)
    @Comment("역할")
    private UserRoleType role;

    @Column(length = 255)
    @Comment("프로필 이미지")
    private String profileImage;

    @Column(length = 20)
    @Comment("상태")
    private UserStatusType status;

    @Column(length = 255)
    @Comment("가입일시")
    private LocalDateTime registeredAt;

    @Column(length = 255)
    @Comment("탈퇴일시")
    private LocalDateTime deletedAt;

    @Column(length = 255)
    @Comment("비고")
    private String note;

    @Builder
    public User(String id, String email, String pwd, String nickname, String phoneNumber,
               UserRoleType role, String profileImage, UserStatusType status,
               LocalDateTime registeredAt, String note) {
        this.id = id;
        this.email = email;
        this.pwd = pwd;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.profileImage = profileImage;
        this.status = status;
        this.registeredAt = registeredAt;
        this.note = note;
    }

    public void update(String email, String nickname, String phoneNumber, 
                      UserRoleType role, String profileImage, UserStatusType status, String note) {
        this.email = email;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.profileImage = profileImage;
        this.status = status;
        this.note = note;
    }

    public void delete(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
        this.status = UserStatusType.DELETED; // 상태도 함께 변경
    }
}