package com.moirai.alloc.user.command.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_users_login_id", columnNames = "login_id"),
                @UniqueConstraint(name = "UK_users_email", columnNames = "email")
        }
)
public class User {

    public enum Status { ACTIVE, SUSPENDED, DELETED }   //유저 상태
    public enum Auth { ADMIN, USER, PM }    //권한

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", length = 100, nullable = false)
    private String loginId;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "user_name", length = 40, nullable = false)
    private String userName;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth")
    private Auth auth;

    @Column(name = "profile_img", length = 100)
    private String profileImg;

    @Builder
    private User(String loginId,
                  String password,
                  String userName,
                  LocalDate birthday,
                  String email,
                  String phone,
                  Auth auth,
                  String profileImg) {
        this.loginId = loginId;
        this.password = password;
        this.userName = userName;
        this.birthday = birthday;
        this.email = email;
        this.phone = phone;
        this.profileImg = profileImg;


        this.status = Status.ACTIVE;
        this.auth = (auth == null) ? Auth.USER : auth;
    }
}
