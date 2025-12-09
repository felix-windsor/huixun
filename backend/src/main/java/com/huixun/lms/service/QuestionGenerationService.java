package com.huixun.lms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huixun.lms.model.ContentFragment;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionGenerationService {

    private final OpenAiChatModel chatModel;
    private final ObjectMapper mapper = new ObjectMapper();

    public QuestionGenerationService(@Value("${app.llm.openai.api-key}") String apiKey,
                                     @Value("${app.llm.openai.model}") String modelName) {
        if (apiKey != null && !apiKey.isBlank()) {
            this.chatModel = OpenAiChatModel.builder().apiKey(apiKey).modelName(modelName).build();
        } else {
            this.chatModel = null;
        }
    }

    public GeneratedQuestion generateFromFragment(ContentFragment f) {
        if (chatModel == null) {
            GeneratedQuestion g = new GeneratedQuestion();
            g.type = "SINGLE";
            g.stem = "根据片段内容的示例题：" + trim(f.getText());
            g.options = List.of("A", "B", "C", "D");
            g.answer = "A";
            g.difficulty = "MEDIUM";
            g.tags = List.of("示例");
            return g;
        }
        String prompt = buildPrompt(f.getText());
        String out = chatModel.generate(prompt);
        try {
            JsonNode n = mapper.readTree(out);
            GeneratedQuestion g = new GeneratedQuestion();
            g.type = n.path("type").asText("SINGLE");
            g.stem = n.path("stem").asText();
            List<String> opts = new ArrayList<>();
            if (n.has("options")) n.get("options").forEach(x -> opts.add(x.asText()));
            g.options = opts;
            g.answer = n.path("answer").asText();
            g.difficulty = n.path("difficulty").asText("MEDIUM");
            List<String> tags = new ArrayList<>();
            if (n.has("knowledgeTags")) n.get("knowledgeTags").forEach(x -> tags.add(x.asText()));
            g.tags = tags;
            return g;
        } catch (Exception e) {
            GeneratedQuestion g = new GeneratedQuestion();
            g.type = "SINGLE";
            g.stem = "题目生成失败，使用回退：" + trim(f.getText());
            g.options = List.of("A", "B", "C", "D");
            g.answer = "A";
            g.difficulty = "EASY";
            g.tags = List.of("回退");
            return g;
        }
    }

    private String buildPrompt(String text) {
        return "你是考试命题专家。根据以下学习片段生成一道题目，并严格输出JSON：\n" +
                "片段：\n" + trim(text) + "\n" +
                "JSON字段：{type:'SINGLE|MULTI|TRUE_FALSE|SHORT_ANSWER',stem:string,options:string[],answer:string,difficulty:'EASY|MEDIUM|HARD',knowledgeTags:string[]}\n" +
                "要求：\n1. 单选题答案为options中的一个\n2. 内容与片段紧密相关\n3. 使用简洁中文\n";
    }

    private String trim(String t) {
        if (t == null) return "";
        String s = t.replaceAll("\s+", " ");
        return s.length() > 180 ? s.substring(0, 180) + "..." : s;
    }

    public static class GeneratedQuestion {
        public String type;
        public String stem;
        public List<String> options;
        public String answer;
        public String difficulty;
        public List<String> tags;
    }
}
