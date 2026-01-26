package com.moirai.alloc.search.query.infra.gpt;

import com.moirai.alloc.search.query.domain.intent.SearchIntent;
import com.moirai.alloc.search.query.domain.intent.SearchIntentMerger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GptIntentRefiner implements SearchIntentParser {
    // 1. rule 결과를 입력으로 받는다
    // 2. 이전 대화상태를 고려한다.
    // 3. 덮어쓰기, 누적을 판단한다.
    // 4. 최종 SearchIntent를 반환한다(다음 searchintent로 정제하는 어뎁터).
    private final RuleBasedIntentParser ruleParser;
    private final SearchIntentMerger merger;
    private final ConversationContext context;

    @Override
    public SearchIntent parse(String nl) {

        //  rule 기반 1차 해석
        SearchIntent ruleIntent = ruleParser.parse(nl);

        // (지금은 GPT 없이) rule 결과를 이전 상태와 병합
        SearchIntent merged =
                merger.merge(
                        context.getLastIntent(),
                        ruleIntent,
                        nl
                );

        // 상태 저장
        context.save(merged);
        return merged;
    }
}