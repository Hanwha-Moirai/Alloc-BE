package com.moirai.alloc.hr.command.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "job_standard")
public class JobStandard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "job_name", length = 100)
    private String jobName;

    @Builder
    private JobStandard(String jobName) {
        this.jobName = jobName;
    }

}
