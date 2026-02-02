package com.moirai.alloc.management.controllerLayerTest;

import com.moirai.alloc.management.command.dto.ProjectSpecParseResponse;
import com.moirai.alloc.management.command.service.EditProject;
import com.moirai.alloc.management.command.service.ProjectSpecIngestService;
import com.moirai.alloc.management.command.service.RegisterProject;
import com.moirai.alloc.management.query.service.GetProjectDetail;
import com.moirai.alloc.management.query.service.GetProjectList;
import com.moirai.alloc.management.query.service.GetProjectRegistrationView;
import com.moirai.alloc.project.command.domain.Project;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(com.moirai.alloc.management.controller.ProjectController.class)
class ProjectControllerTest extends ControllerTestSupport {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    GetProjectList getProjectList;
    @MockBean
    GetProjectDetail getProjectDetail;
    @MockBean
    RegisterProject registerProject;
    @MockBean
    GetProjectRegistrationView getProjectRegistrationView;
    @MockBean
    EditProject editProject;
    @MockBean
    ProjectSpecIngestService projectSpecIngestService;

    @Test
    void uploadProjectSpec_returnsOk_forPmUser() throws Exception {
        ProjectSpecParseResponse response = ProjectSpecParseResponse.builder()
                .projectId(1L)
                .projectName("테스트 프로젝트")
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .predictedCost(100_000_000)
                .partners("테스트 고객사")
                .description("테스트 설명")
                .projectType(Project.ProjectType.NEW)
                .projectStatus(Project.ProjectStatus.DRAFT)
                .pageCount(3)
                .build();

        when(projectSpecIngestService.ingest(anyLong(), any())).thenReturn(response);

        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "spec.pdf",
                "application/pdf",
                "dummy".getBytes()
        );

        mockMvc.perform(multipart("/api/projects/1/docs/project-spec")
                        .file(pdfFile)
                        .with(csrf())
                        .with(authenticatedUser("PM"))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    @Test
    void uploadProjectSpec_returnsForbidden_forNonPmUser() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "spec.pdf",
                "application/pdf",
                "dummy".getBytes()
        );

        mockMvc.perform(multipart("/api/projects/1/docs/project-spec")
                        .file(pdfFile)
                        .with(csrf())
                        .with(authenticatedUser("USER"))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
    }
}
