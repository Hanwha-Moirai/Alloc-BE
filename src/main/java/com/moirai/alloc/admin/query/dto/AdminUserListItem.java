package com.moirai.alloc.admin.query.dto;

import com.moirai.alloc.user.command.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserListItem {
    private final Long userId;
    private final String userName;
    private final String email;
    private final String auth;   // ADMIN/PM/USER
    private final String status; // ACTIVE/SUSPENDED/DELETED

    public static AdminUserListItem from(User user) {
        return AdminUserListItem.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .auth(user.getAuth().name())
                .status(user.getStatus().name())
                .build();
    }
}
