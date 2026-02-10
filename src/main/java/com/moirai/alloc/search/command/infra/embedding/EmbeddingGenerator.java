package com.moirai.alloc.search.command.infra.embedding;

public interface EmbeddingGenerator {
    float[] generate(String text);
}