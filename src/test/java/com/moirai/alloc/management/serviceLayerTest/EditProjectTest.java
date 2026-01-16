package com.moirai.alloc.management.serviceLayerTest;

import com.moirai.alloc.management.command.dto.EditProjectDTO;
import com.moirai.alloc.management.command.service.EditProject;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.project.command.domain.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
@ExtendWith(MockitoExtension.class)
class EditProjectTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private EditProject editProject;

    @Test
    void updateUpdatesProjectBasicInfo() {
        // given
        Project project = Project.builder()
                .name("기존 프로젝트")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 6, 30))
                .partners("기존 고객")
                .description("기존 설명")
                .predictedCost(1000)
                .build();

        ReflectionTestUtils.setField(project, "projectId", 1L);

        given(projectRepository.findById(1L))
                .willReturn(Optional.of(project));

        EditProjectDTO command = new EditProjectDTO();
        ReflectionTestUtils.setField(command, "projectId", 1L);
        ReflectionTestUtils.setField(command, "projectName", "수정된 프로젝트");
        ReflectionTestUtils.setField(command, "startDate", LocalDate.of(2025, 2, 1));
        ReflectionTestUtils.setField(command, "endDate", LocalDate.of(2025, 7, 31));
        ReflectionTestUtils.setField(command, "partners", "수정 고객");
        ReflectionTestUtils.setField(command, "description", "수정 설명");
        ReflectionTestUtils.setField(command, "predictedCost", 2000);

        // when
        editProject.update(command);

        // then
        assertThat(project.getName()).isEqualTo("수정된 프로젝트");
        assertThat(project.getPredictedCost()).isEqualTo(2000);
        assertThat(project.getPartners()).isEqualTo("수정 고객");
        assertThat(project.getStartDate()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(project.getEndDate()).isEqualTo(LocalDate.of(2025, 7, 31));
    }
}
