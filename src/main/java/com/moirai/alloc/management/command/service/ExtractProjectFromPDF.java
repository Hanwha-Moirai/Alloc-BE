package com.moirai.alloc.management.command.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExtractProjectFromPDF {
    // PDF에서 내용을 추출한다.
    private final RegisterProjectFromPDF registerProjectFromPDF;

}
