package com.moirai.alloc.management.domain.policy.scoring;

import com.moirai.alloc.project.command.domain.Project;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class WeightPolicy {

    private static final Map<Project.ProjectType, ScoreWeight> WEIGHTS =
            Map.of(
                    Project.ProjectType.NEW,
                    new ScoreWeight(0.45, 0.30, 0.15),

                    Project.ProjectType.OPERATION,
                    new ScoreWeight(0.30, 0.40, 0.20),

                    Project.ProjectType.MAINTENANCE,
                    new ScoreWeight(0.25, 0.30, 0.30)
            );

    public int apply(Project project, CandidateScore score) {

        ScoreWeight weight =
                WEIGHTS.getOrDefault(
                        project.getProjectType(),
                        WEIGHTS.get(Project.ProjectType.NEW) // 기본값
                );

        double weightedTotal =
                score.getSkillScore() * weight.getSkill()
                        + score.getExperienceScore() * weight.getExperience()
                        + score.getAvailabilityScore() * weight.getAvailability();

        return (int) Math.round(weightedTotal);
    }
    public ScoreWeight getBaseWeight(Project project) {
        return WEIGHTS.getOrDefault(
                project.getProjectType(),
                WEIGHTS.get(Project.ProjectType.NEW)
        );
    }
    public int apply(CandidateScore score, ScoreWeight weight) {

        double weightedTotal =
                score.getSkillScore() * weight.getSkill()
                        + score.getExperienceScore() * weight.getExperience()
                        + score.getAvailabilityScore() * weight.getAvailability();

        return (int) Math.round(weightedTotal);
    }


}
//CandidateScore + ProjectType → 가중치 적용된 최종 점수
// 프로젝트 타입별 해석 로직
