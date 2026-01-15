package com.moirai.alloc.common.dto.pagination;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ScrollPaginationCollection<T, C> {

    private final List<T> itemsWithNext;
    private final int size;
    private final java.util.function.Function<T, C> cursorExtractor;

    public static <T, C> ScrollPaginationCollection<T, C> of(
            List<T> itemsWithNext, int size, java.util.function.Function<T, C> cursorExtractor
    ) {
        return new ScrollPaginationCollection<>(itemsWithNext, size, cursorExtractor);
    }

    public boolean isLastScroll() {
        return itemsWithNext.size() <= size;
    }

    public List<T> getCurrentItems() {
        return isLastScroll() ? itemsWithNext : itemsWithNext.subList(0, size);
    }

    /* 다음 요청에 사용할 커서 = "이번에 내려준 마지막 아이템" 기준 */
    public C getNextCursor() {
        if (getCurrentItems().isEmpty()) return null;
        T last = getCurrentItems().get(getCurrentItems().size() - 1);
        return cursorExtractor.apply(last);
    }
}

