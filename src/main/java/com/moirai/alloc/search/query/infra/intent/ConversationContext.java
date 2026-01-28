package com.moirai.alloc.search.query.infra.intent;

import com.moirai.alloc.search.query.domain.intent.SearchIntent;
import com.moirai.alloc.search.query.infra.openSearch.PersonDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConversationContext {

    private final Map<String, SearchIntent> intentStore = new ConcurrentHashMap<>();
    private final Map<String, List<PersonDocument>> resultStore = new ConcurrentHashMap<>();
    private final Map<String, Integer> questionCountStore = new ConcurrentHashMap<>();
    public SearchIntent getLastIntent(String conversationId) {
        return intentStore.get(conversationId);
    }

    public void saveIntent(String conversationId, SearchIntent intent) {
        intentStore.put(conversationId, intent);
    }

    public List<PersonDocument> getLastResult(String conversationId) {
        return resultStore.get(conversationId);
    }

    public void saveResult(String conversationId, List<PersonDocument> result) {
        resultStore.put(conversationId, result);
    }
    public int getQuestionCount(String conversationId) {
        return questionCountStore.getOrDefault(conversationId, 0);
    }

    public int incrementQuestionCount(String conversationId) {
        return questionCountStore.merge(conversationId, 1, Integer::sum);
    }

    public void reset(String conversationId) {
        intentStore.remove(conversationId);
        resultStore.remove(conversationId);
        questionCountStore.remove(conversationId);
    }
}
