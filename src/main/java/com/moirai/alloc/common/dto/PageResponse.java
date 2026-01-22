package com.moirai.alloc.common.dto;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> { //<ResponseDTO> 어떤 ResponseDto든 넣어서 만들면 됨

    //실제 데이터 리스트
    private List<T> content;
    //현재 페이지 번호
    private int currentPage;
    //전체 페이지 수
    private int totalPages;
    //전체 데이터 수
    private long totalElements;
    //페이지 당 데이터 수
    private int size;

    //Service에서 JPA Page 객체를 DTO나 Controller에서 바로 JSON으로 변환할 수 있음
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .build();
    }

    public static <T> PageResponse<T> from(List<T> content, int page, int size, int totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, totalPages, totalElements, size);
    }
}