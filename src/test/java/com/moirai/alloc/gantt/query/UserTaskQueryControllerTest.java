package com.moirai.alloc.gantt.query;

import com.moirai.alloc.gantt.command.domain.entity.Task.TaskCategory;
import com.moirai.alloc.gantt.command.domain.entity.Task.TaskStatus;
import com.moirai.alloc.gantt.query.application.GanttQueryService;
import com.moirai.alloc.gantt.query.controller.UserTaskQueryController;
import com.moirai.alloc.gantt.query.dto.response.TaskResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserTaskQueryController.class)
class UserTaskQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GanttQueryService ganttQueryService;

    @Test
    @WithMockUser
    @DisplayName("사용자 미완료 태스크 목록 조회가 성공한다.")
    void findIncompleteTasksByUser_returnsOnlyIncompleteTasks() throws Exception {
        List<TaskResponse> responses = List.of(
                new TaskResponse(
                        99001L,
                        99001L,
                        "User Two",
                        TaskCategory.DEVELOPMENT,
                        "Seed Task 1",
                        "desc",
                        TaskStatus.TODO,
                        LocalDateTime.of(2025, 1, 2, 0, 0),
                        LocalDateTime.of(2025, 1, 2, 0, 0),
                        LocalDate.of(2025, 1, 2),
                        LocalDate.of(2025, 1, 5),
                        false,
                        false
                )
        );
        given(ganttQueryService.findIncompleteTasksByUserId(99002L)).willReturn(responses);

        mockMvc.perform(get("/api/users/{userId}/tasks", 99002))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[*].taskId", hasItem(99001)))
                .andExpect(jsonPath("$.data[*].taskId", not(hasItem(99002))));
    }
}
