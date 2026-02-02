package com.moirai.alloc.management.controllerLayerTest;

import com.moirai.alloc.management.domain.repo.ProjectDocumentRepository;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.command.service.EditProject;
import com.moirai.alloc.project.command.domain.Project;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ProjectPdfUploadControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProjectRepository projectRepository;
    @MockitoBean
    ProjectDocumentRepository projectDocumentRepository;
    @MockitoBean
    EditProject editProject;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("pdf.service.base-url", () -> "http://192.168.64.5:8010");
    }

    @Test
    void uploadProjectSpec_callsPdfService_andUpdatesProject() throws Exception {
        Project project = Mockito.mock(Project.class);
        when(project.getName()).thenReturn("기존 프로젝트");
        when(project.getProjectType()).thenReturn(Project.ProjectType.NEW);
        when(project.getProjectStatus()).thenReturn(Project.ProjectStatus.DRAFT);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        MockMultipartFile pdfFile = loadSamplePdf();

        mockMvc.perform(multipart("/api/projects/1/docs/project-spec")
                        .file(pdfFile)
                        .with(csrf())
                        .with(csrfToken())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(pmAuth()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        verify(editProject).update(any());

    }

    private MockMultipartFile loadSamplePdf() throws IOException {
        ClassPathResource resource = new ClassPathResource("[SION] 프로젝트 기획서.pdf");
        byte[] content = resource.getInputStream().readAllBytes();
        return new MockMultipartFile(
                "file",
                resource.getFilename(),
                "application/pdf",
                content
        );
    }

    private RequestPostProcessor csrfToken() {
        return request -> {
            String token = "test-csrf-token";
            request.setCookies(new Cookie("csrfToken", token));
            request.addHeader("X-CSRF-Token", token);
            return request;
        };
    }

    private Authentication pmAuth() {
        UserPrincipal principal = new UserPrincipal(
                1L,
                "pm_1",
                "pm1@example.com",
                "PM User",
                "PM",
                "pw"
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
