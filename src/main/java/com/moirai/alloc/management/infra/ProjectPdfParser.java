package com.moirai.alloc.management.infra;

import com.moirai.alloc.management.command.dto.RegisterProjectCommandDTO;
import org.springframework.stereotype.Component;

@Component
public class ProjectPdfParser {
    // 텍스트 -> 프로젝트 필드 파싱
    public RegisterProjectCommandDTO parseProjectPdf(String text) {


        return null;
    }

}
// 프로젝트 등록시 작성하는 내용
//    private String name;
//    private LocalDate startDate;
//    private LocalDate endDate;
//    private String partners;
//    private Integer predictedCost;
//    private Project.ProjectType projectType;
//    private String description;
