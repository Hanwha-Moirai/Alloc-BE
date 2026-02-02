package com.moirai.alloc.management.command.service;

import com.moirai.alloc.management.client.PdfServiceClient;
import com.moirai.alloc.management.command.dto.EditProjectDTO;
import com.moirai.alloc.management.command.dto.PdfExtractResponse;
import com.moirai.alloc.management.command.dto.ProjectSpecParseResponse;
import com.moirai.alloc.management.domain.entity.ProjectDocument;
import com.moirai.alloc.management.domain.repo.ProjectDocumentRepository;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProjectSpecIngestService {

    private final PdfServiceClient pdfServiceClient;
    private final ProjectRepository projectRepository;
    private final ProjectDocumentRepository projectDocumentRepository;
    private final EditProject editProject;

    private final ProjectSpecParser parser = new ProjectSpecParser();

    @Transactional
    public ProjectSpecParseResponse ingest(Long projectId, MultipartFile file) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        PdfExtractResponse extractResponse = pdfServiceClient.extractText(file);
        String extractedText = extractResponse == null ? "" : extractResponse.getText();

        ProjectSpecMetadata metadata = parser.parse(extractedText);

        EditProjectDTO command = new EditProjectDTO();
        command.setProjectId(projectId);
        command.setProjectName(firstNonNull(metadata.name(), project.getName()));
        command.setStartDate(firstNonNull(metadata.startDate(), project.getStartDate()));
        command.setEndDate(firstNonNull(metadata.endDate(), project.getEndDate()));
        command.setPredictedCost(firstNonNull(metadata.predictedCost(), project.getPredictedCost()));
        command.setPartners(firstNonNull(metadata.partners(), project.getPartners()));
        command.setDescription(firstNonNull(metadata.description(), project.getDescription()));
        command.setProjectType(firstNonNull(metadata.projectType(), project.getProjectType()));
        command.setProjectStatus(firstNonNull(metadata.projectStatus(), project.getProjectStatus()));

        editProject.update(command);

        if (extractResponse != null) {
            String fileName = extractResponse.getFileName() == null ? file.getOriginalFilename() : extractResponse.getFileName();
            projectDocumentRepository.save(new ProjectDocument(fileName, extractedText));
        }

        return ProjectSpecParseResponse.builder()
                .projectId(projectId)
                .projectName(command.getProjectName())
                .startDate(command.getStartDate())
                .endDate(command.getEndDate())
                .predictedCost(command.getPredictedCost())
                .partners(command.getPartners())
                .description(command.getDescription())
                .projectType(command.getProjectType())
                .projectStatus(command.getProjectStatus())
                .pageCount(extractResponse == null ? 0 : extractResponse.getPageCount())
                .build();
    }

    private <T> T firstNonNull(T primary, T fallback) {
        return primary != null ? primary : fallback;
    }
}
