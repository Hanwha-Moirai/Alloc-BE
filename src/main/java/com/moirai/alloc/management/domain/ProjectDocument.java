package com.moirai.alloc.management.domain;


import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    protected ProjectDocument() {}

    public ProjectDocument(String filePath, String extractedText) {
        this.filePath = filePath;
        this.extractedText = extractedText;
        this.uploadedAt = LocalDateTime.now();
    }
}
