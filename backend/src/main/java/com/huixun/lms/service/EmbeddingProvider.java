package com.huixun.lms.service;

import java.util.List;

public interface EmbeddingProvider {
    List<Double> embed(String text);
}
