package com.moirai.alloc.hr.command.domain;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "title_standard")
public class TitleStandard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "title_standard_id")
    private Long titleStandardId;

    @Column(name = "title_name", nullable = false, length = 250)
    private String titleName;

    @Column(name = "monthly_cost")
    private Integer monthlyCost;

    @Builder
    private TitleStandard(String titleName, Integer monthlyCost) {
        this.titleName = titleName;
        this.monthlyCost = monthlyCost;
    }

}