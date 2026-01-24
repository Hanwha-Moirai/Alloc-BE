import com.moirai.alloc.search.query.domain.intent.SearchIntent;
import com.moirai.alloc.search.query.infra.gpt.GptIntentRefiner;
import com.moirai.alloc.search.query.infra.gpt.SearchIntentParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntentMerger implements SearchIntentParser {

    private final GptIntentRefiner gptParser;

    @Override
    public SearchIntent parse(String nl) {

        SearchIntent ruleIntent = ruleParser.parse(nl);

        if (isEnough(ruleIntent)) {
            return ruleIntent;
        }

        SearchIntent gptIntent = gptParser.parse(nl);
        return merge(ruleIntent, gptIntent);
    }

    private boolean isEnough(SearchIntent intent) {
        return intent.getTechs() != null && !intent.getTechs().isEmpty();
    }

    private SearchIntent merge(SearchIntent rule, SearchIntent gpt) {
        return SearchIntent.builder()
                .freeText(rule.getFreeText())
                .techs(rule.getTechs() != null ? rule.getTechs() : gpt.getTechs())
                .skillLevel(rule.getSkillLevel() != null ? rule.getSkillLevel() : gpt.getSkillLevel())
                .seniorityLevel(rule.getSeniorityLevel() != null ? rule.getSeniorityLevel() : gpt.getSeniorityLevel())
                .activeProjectCount(rule.getActiveProjectCount() != null ? rule.getActiveProjectCount() : gpt.getActiveProjectCount())
                .comparisonType(rule.getComparisonType() != null ? rule.getComparisonType() : gpt.getComparisonType())
                .department(rule.getDepartment() != null ? rule.getDepartment() : gpt.getDepartment())
                .limit(gpt.getLimit())
                .build();
    }
}
