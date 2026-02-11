package com.moirai.alloc.search.command.infra.embedding;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OpenAiEmbeddingResponse {

    private List<Data> data;

    @Getter
    @Setter
    public static class Data {
        private List<Double> embedding;
    }
}