package com.moirai.alloc.user.query.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserListItem {
    private final Long userId;
    private final String userName;
    private final String email;
    private final String auth;   // ADMIN/PM/USER
    private final String status; // ACTIVE/SUSPENDED/DELETED
}
