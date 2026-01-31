package com.moirai.alloc.search.query.domain.condition;

import com.moirai.alloc.search.query.domain.vocabulary.JobGrade;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JobGradeRange {
    private JobGrade minGrade;
    private JobGrade maxGrade;

}
