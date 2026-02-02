package com.moirai.alloc.management.command.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PdfExtractResponse {
    private String fileName;
    private int pageCount;
    private String text;
}
