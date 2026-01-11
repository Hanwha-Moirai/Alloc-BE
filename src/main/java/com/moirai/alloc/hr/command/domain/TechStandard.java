package com.moirai.alloc.hr.command.domain;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "tech_standard",
        uniqueConstraints = @UniqueConstraint(columnNames = "tech_name")
)
public class TechStandard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tech_id")
    private Long techId;

    @Column(name = "tech_name", nullable = false, length = 50)
    private String techName;

    @Builder
    private TechStandard(String techName) {
        this.techName = techName;
    }

}
