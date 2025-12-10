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
                                     @Value("${app.llm.openai.chat-model}") String modelName) {
        if (apiKey != null && !apiKey.isBlank()) {
            this.chatModel = OpenAiChatModel.builder().apiKey(apiKey).modelName(modelName).build();
        } else {
            this.chatModel = null;
        }
    }

    public GeneratedQuestion generateFromFragment(ContentFragment f) {
        return generateFromFragment(f, null);
    }

    public GeneratedQuestion generateFromFragment(ContentFragment f, String typePreference) {
        if (chatModel == null) {
            GeneratedQuestion g = new GeneratedQuestion();
            g.type = "SINGLE";
            g.stem = "阅读以下片段，选择最符合片段主要内容的选项:\n" + trim(f.getText());
            g.options = List.of("与片段主要内容一致", "与片段无关", "与片段部分相关", "无法判断");
            g.answer = "与片段主要内容一致";
            g.difficulty = "MEDIUM";
            g.tags = List.of("示例");
            return adaptType(g, typePreference, f.getText());
        }
        try {
            String prompt = buildPrompt(f.getText());
            String out = chatModel.generate(prompt);
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
            g = validate(g, f.getText());
            return adaptType(g, typePreference, f.getText());
        } catch (Exception e) {
            GeneratedQuestion g = new GeneratedQuestion();
            g.type = "SINGLE";
            g.stem = "根据片段内容选择最符合的陈述:\n" + trim(f.getText());
            List<String> opts = buildFallbackOptions(f.getText());
            g.options = opts;
            g.answer = opts.isEmpty() ? "" : opts.get(0);
            g.difficulty = "EASY";
            g.tags = fallbackTags(f.getText());
            return adaptType(g, typePreference, f.getText());
        }
    }

    private GeneratedQuestion adaptType(GeneratedQuestion g, String typePreference, String sourceText) {
        if (typePreference == null || typePreference.isBlank()) return g;
        String tp = typePreference.trim().toUpperCase();
        if ("TRUE_FALSE".equals(tp)) {
            g.type = "TRUE_FALSE";
            g.options = List.of("正确", "错误");
            String src = sourceText == null ? "" : sourceText;
            String a = g.answer == null ? "" : g.answer;
            String ref = a.replace("…", "");
            g.answer = (!ref.isBlank() && src.contains(ref)) ? "正确" : "错误";
            if (g.stem == null || g.stem.isBlank()) g.stem = "判断以下陈述是否正确:\n" + deriveStatement(sourceText);
        } else if ("MULTI".equals(tp)) {
            g.type = "MULTI";
            List<String> opts = g.options;
            if (opts == null || opts.size() < 4) opts = buildFallbackOptions(sourceText);
            g.options = opts;
            int desired = desiredMultiCorrectCount(g.difficulty);
            List<Integer> correct = new ArrayList<>();
            String src = sourceText == null ? "" : sourceText;
            for (int i = 0; i < opts.size(); i++) {
                String o = opts.get(i);
                if (o != null && !o.isBlank()) {
                    String ref = o.replace("…", "");
                    if (!ref.isBlank() && src.contains(ref)) correct.add(i);
                }
            }
            java.util.Set<Integer> used = new java.util.LinkedHashSet<>(correct);
            for (int i = 0; i < opts.size() && used.size() < desired; i++) used.add(i);
            List<Integer> finalIdx = new ArrayList<>(used).subList(0, Math.min(desired, used.size()));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < finalIdx.size(); i++) {
                int idx = finalIdx.get(i);
                char ch = (char) ('A' + idx);
                if (i > 0) sb.append(',');
                sb.append(ch);
            }
            g.answer = sb.toString();
        } else {
            g.type = "SINGLE";
        }
        return g;
    }

    private List<String> buildFallbackOptions(String text) {
        List<String> sents = splitSentences(text);
        String main = sents.stream().filter(x -> x.length() >= 8).findFirst().orElse(trim(text));
        String s1 = shorten(main);
        String s2 = shorten(mutate(main));
        String s3 = sents.size() > 1 ? shorten(sents.get(1)) : shorten(mutate(main + "内容"));
        String s4 = sents.size() > 2 ? shorten(sents.get(2)) : shorten(mutate(main + "目标"));
        List<String> opts = new ArrayList<>();
        for (String s : List.of(s1, s2, s3, s4)) {
            if (s != null && !s.isBlank() && opts.stream().noneMatch(o -> o.equals(s))) opts.add(s);
        }
        while (opts.size() < 4) opts.add(shorten(mutate(main + opts.size())));
        return opts.subList(0, Math.min(4, opts.size()));
    }

    private List<String> splitSentences(String t) {
        if (t == null) return java.util.Collections.emptyList();
        String s = t.replaceAll("\s+", " ");
        String[] parts = s.split("[。！？.!?]\s*");
        List<String> out = new ArrayList<>();
        for (String p : parts) { String x = p.trim(); if (!x.isEmpty()) out.add(x); }
        return out;
    }

    private String shorten(String s) {
        if (s == null) return "";
        return s.length() > 30 ? s.substring(0, 30) + "…" : s;
    }

    private String mutate(String s) {
        if (s == null) return "";
        String x = s;
        x = x.replace("必须", "可以");
        x = x.replace("应当", "不应");
        x = x.replace("禁止", "允许");
        x = x.replace("加强", "削弱");
        x = x.replace("提高", "降低");
        x = x.replace("促进", "限制");
        return x;
    }

    private List<String> fallbackTags(String t) {
        if (t == null) return java.util.Collections.emptyList();
        String s = t.replaceAll("\s+", " ");
        java.util.Set<String> set = new java.util.LinkedHashSet<>();
        String[] parts = s.split("[，。；,;]\s*");
        for (String p : parts) {
            String w = p.trim();
            if (w.length() >= 4) set.add(w.substring(0, Math.min(8, w.length())));
            if (set.size() >= 3) break;
        }
        return new java.util.ArrayList<>(set);
    }

    private String buildPrompt(String text) {
        return "你是考试命题专家。根据以下学习片段生成一道中文单选题，严格返回JSON且不包含任何额外文本：\n" +
                "片段：\n" + trim(text) + "\n" +
                "JSON字段：{type:'SINGLE',stem:string,options:string[],answer:string,difficulty:'EASY|MEDIUM|HARD',knowledgeTags:string[]}\n" +
                "规则：\n" +
                "1. 生成4个不同且具体的中文选项，只有1个为正确项，其余为贴近但错误的干扰项。\n" +
                "2. 不允许使用泛化选项，例如‘与片段无关’或‘无法判断’。\n" +
                "3. 每个选项不超过30字，answer必须等于options中的完整字符串。\n" +
                "4. stem简洁明确，knowledgeTags基于片段主题词。\n";
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

    private GeneratedQuestion validate(GeneratedQuestion g, String sourceText) {
        if (g.options == null) g.options = new ArrayList<>();
        List<String> uniq = new ArrayList<>();
        java.util.Set<String> set = new java.util.LinkedHashSet<>();
        for (String o : g.options) { if (o != null) set.add(o); }
        uniq.addAll(set);
        if (uniq.size() < 4) uniq = buildFallbackOptions(sourceText);
        if (uniq == null || uniq.isEmpty()) uniq = List.of("选项A", "选项B", "选项C", "选项D");
        g.options = uniq.size() >= 4 ? uniq.subList(0, 4) : uniq;
        if (g.answer == null || g.answer.isBlank() || g.options.stream().noneMatch(o -> o.equals(g.answer))) {
            g.answer = g.options.isEmpty() ? "" : g.options.get(0);
        }
        String d = g.difficulty == null ? "" : g.difficulty.trim().toUpperCase();
        if (!List.of("EASY", "MEDIUM", "HARD").contains(d)) d = "MEDIUM";
        g.difficulty = d;
        if (g.stem == null) g.stem = "";
        if (g.stem.isBlank()) g.stem = "阅读以下片段，选择最符合片段主要内容的选项:\n" + trim(sourceText);
        if (g.tags == null) g.tags = fallbackTags(sourceText);
        return g;
    }

    private int desiredMultiCorrectCount(String difficulty) {
        String d = difficulty == null ? "" : difficulty.trim().toUpperCase();
        if ("EASY".equals(d)) return 1;
        if ("HARD".equals(d)) return 3;
        return 2;
    }

    private String deriveStatement(String text) {
        List<String> sents = splitSentences(text);
        String main = sents.stream().filter(x -> x.length() >= 8).findFirst().orElse(trim(text));
        return shorten(main);
    }
}
