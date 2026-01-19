package com.moirai.alloc.management.command.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterProjectFromPDF {
    // PDF를 등록한다.
    private final RegisterProject registerProject;
    private final ExtractProjectFromPDF extractProjectFromPDF;

}
