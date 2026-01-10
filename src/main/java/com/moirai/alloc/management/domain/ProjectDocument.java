package com.moirai.alloc.management.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "project_document")
public class ProjectDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long docId;

    @Column(nullable = false)
    private String filePath;

    @Lob
    @Column(nullable = false)
    private String extractedText;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    public ProjectDocument(String filePath, String extractedText) {
        this.filePath = filePath;
        this.extractedText = extractedText;
        this.uploadedAt = LocalDateTime.now();
    }
}
