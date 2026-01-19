package com.moirai.alloc.management.serviceLayerTest;

import com.moirai.alloc.management.command.dto.RegisterProjectCommandDTO;
import com.moirai.alloc.management.command.service.AssignProjectManager;
import com.moirai.alloc.management.command.service.RegisterProject;
import com.moirai.alloc.management.domain.entity.TechReqLevel;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.domain.vo.TechRequirement;
import com.moirai.alloc.project.command.domain.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.assertThat;
// 시나리오: 프로젝트 등록 시, 프로젝트 저장하고 PM을 자동 배정한다.
@ExtendWith(MockitoExtension.class)
class RegisterProjectTest {

    @InjectMocks
    private RegisterProject registerProject;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AssignProjectManager assignProjectManager;

    @Test
    void registerProject() {
        // given
        RegisterProjectCommandDTO command = new RegisterProjectCommandDTO();

        ReflectionTestUtils.setField(command, "name", "AI 프로젝트");
        ReflectionTestUtils.setField(command, "startDate", LocalDate.of(2025, 1, 1));
        ReflectionTestUtils.setField(command, "endDate", LocalDate.of(2025, 6, 30));
        ReflectionTestUtils.setField(command, "partners", "삼성");
        ReflectionTestUtils.setField(command, "predictedCost", 1_000_000);
        ReflectionTestUtils.setField(command, "projectType", Project.ProjectType.NEW);
        ReflectionTestUtils.setField(command, "description", "AI 기반 추천 시스템");

        ReflectionTestUtils.setField(command, "jobRequirements", List.of(
                new JobRequirement(1L, 2),
                new JobRequirement(2L, 1)
        ));

        ReflectionTestUtils.setField(command, "techRequirements", List.of(
                new TechRequirement(10L, TechReqLevel.LV1),
                new TechRequirement(20L, TechReqLevel.LV3)
        ));

        Project savedProject = Project.builder()
                .name("AI 프로젝트")
                .build();
        ReflectionTestUtils.setField(savedProject, "projectId", 100L);

        when(projectRepository.save(any(Project.class)))
                .thenReturn(savedProject);
        Long pmUserId = 1L;

        // when
        Long projectId = registerProject.registerProject(command,1L);

        // 반환된 PROJECTID 검증
        assertThat(projectId).isEqualTo(100L);
        // 프로젝트 저장 시 전달 된 값 검증
        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());

        Project project = captor.getValue();
        assertThat(project.getName()).isEqualTo("AI 프로젝트");
        assertThat(project.getJobRequirements()).hasSize(2);
        assertThat(project.getTechRequirements()).hasSize(2);

        // 도메인 정책 검증; 프로젝트 저장 후 생성자를 PM로 자동 배정하는 정책이 실행되는지
        verify(assignProjectManager)
                .assignPm(100L, pmUserId);
    }
}
