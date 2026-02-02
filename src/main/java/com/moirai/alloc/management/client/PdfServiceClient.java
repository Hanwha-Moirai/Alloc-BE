package com.moirai.alloc.management.client;

import com.moirai.alloc.management.command.dto.PdfExtractResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PdfServiceClient {

    private final WebClient pdfServiceWebClient;

    public PdfExtractResponse extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is required.");
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        try {
            builder.part("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            }).contentType(MediaType.APPLICATION_PDF);
        } catch (IOException exc) {
            throw new IllegalStateException("Failed to read PDF file.", exc);
        }

        MultiValueMap<String, org.springframework.http.HttpEntity<?>> parts = builder.build();

        try {
            return pdfServiceWebClient.post()
                    .uri("/pdf/extract")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(parts))
                    .retrieve()
                    .bodyToMono(PdfExtractResponse.class)
                    .block();
        } catch (WebClientResponseException exc) {
            throw new IllegalStateException("PDF extract failed: " + exc.getResponseBodyAsString(), exc);
        }
    }
}
