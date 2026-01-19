package com.moirai.alloc.admin.query.service;

import com.moirai.alloc.admin.query.dto.AdminTechStackListItem;
import com.moirai.alloc.admin.query.dto.AdminUserListItem;
import com.moirai.alloc.admin.query.dto.AdminUserMetaResponse;
import com.moirai.alloc.common.dto.PageResponse;
import com.moirai.alloc.admin.query.mapper.AdminUserQueryMapper;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.user.command.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserQueryService {

    private final AdminUserQueryMapper mapper;

    //사용자 조회
    public PageResponse<AdminUserListItem> getUsers(int page, int size, String q, String role, String status) {

        int pageNo = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        int offset = (pageNo - 1) * pageSize;

        List<AdminUserListItem> content = mapper.selectUsers(pageSize, offset, q, role, status);
        long total = mapper.countUsers(q, role, status);

        return PageResponse.from(content, pageNo, pageSize, (int) total);
    }

    public AdminUserMetaResponse getUserMeta() {
        return AdminUserMetaResponse.builder()
                .employeeTypes(toEmployeeTypeOptions())
                .auths(toOptions(User.Auth.values()))
                .statuses(toOptions(User.Status.values()))
                .build();
    }

    // 근무형태 전용 (한글 매핑)
    private List<AdminUserMetaResponse.CodeLabel> toEmployeeTypeOptions() {
        return Arrays.stream(Employee.EmployeeType.values())
                .map(type -> AdminUserMetaResponse.CodeLabel.builder()
                        .code(type.name())
                        .label(mapEmployeeTypeLabel(type))
                        .build()
                )
                .toList();
    }

    // auth, status 공용 (enum 그대로)
    private List<AdminUserMetaResponse.CodeLabel> toOptions(Enum<?>[] values) {
        return Arrays.stream(values)
                .map(e -> AdminUserMetaResponse.CodeLabel.builder()
                        .code(e.name())
                        .label(e.name())
                        .build()
                )
                .toList();
    }

    private String mapEmployeeTypeLabel(Employee.EmployeeType type) {
        return switch (type) {
            case FULL_TIME -> "정규직";
            case CONTRACT -> "계약직";
            case INTERN -> "인턴";
            case VENDOR -> "외주";
        };
    }

}