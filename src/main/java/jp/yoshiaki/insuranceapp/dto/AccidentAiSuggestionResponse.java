package jp.yoshiaki.insuranceapp.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 事故AI提案の画面表示用DTO。
 */
public record AccidentAiSuggestionResponse(
        String summary,
        List<String> actions,
        List<String> cautions) implements Serializable {

    private static final String SUMMARY_HEADING = "概要:";
    private static final String ACTIONS_HEADING = "対応項目:";
    private static final String CAUTIONS_HEADING = "注意点:";

    public static AccidentAiSuggestionResponse from(String raw) {
        if (raw == null || raw.isBlank()) {
            return fallback();
        }

        String summary = "";
        List<String> actions = new ArrayList<>();
        List<String> cautions = new ArrayList<>();
        Section currentSection = Section.NONE;

        for (String rawLine : raw.lines().toList()) {
            String line = clean(rawLine);
            if (line.isBlank()) {
                continue;
            }

            if (line.startsWith(SUMMARY_HEADING)) {
                currentSection = Section.SUMMARY;
                summary = line.substring(SUMMARY_HEADING.length()).trim();
                continue;
            }
            if (line.startsWith(ACTIONS_HEADING)) {
                currentSection = Section.ACTIONS;
                continue;
            }
            if (line.startsWith(CAUTIONS_HEADING)) {
                currentSection = Section.CAUTIONS;
                String caution = line.substring(CAUTIONS_HEADING.length()).trim();
                if (!caution.isBlank()) {
                    cautions.add(caution);
                }
                continue;
            }

            switch (currentSection) {
                case SUMMARY -> summary = appendSentence(summary, line);
                case ACTIONS -> actions.add(stripListMarker(line));
                case CAUTIONS -> cautions.add(stripListMarker(line));
                default -> actions.add(stripListMarker(line));
            }
        }

        actions = actions.stream().map(AccidentAiSuggestionResponse::clean).filter(s -> !s.isBlank()).toList();
        cautions = cautions.stream().map(AccidentAiSuggestionResponse::clean).filter(s -> !s.isBlank()).toList();

        if (summary.isBlank()) {
            summary = "事故情報に基づく次アクション候補です。";
        }
        if (actions.isEmpty() && cautions.isEmpty()) {
            return fallback();
        }

        return new AccidentAiSuggestionResponse(summary, actions, cautions);
    }

    private static AccidentAiSuggestionResponse fallback() {
        return new AccidentAiSuggestionResponse(
                "事故情報に基づく次アクション候補です。",
                List.of("対応履歴を確認し、次に必要な連絡・確認事項を整理してください。"),
                List.of());
    }

    private static String appendSentence(String current, String additional) {
        if (current.isBlank()) {
            return additional;
        }
        return current + " " + additional;
    }

    private static String clean(String value) {
        return value == null ? "" : value
                .trim()
                .replace("**", "")
                .replaceAll("^\\s*[・*\\-]\\s*", "")
                .replaceAll("^\\s*[0-9０-９]+[.)．、]\\s*", "")
                .replaceAll("^\\s*[①②③④⑤⑥⑦⑧⑨⑩]\\s*", "")
                .trim();
    }

    private static String stripListMarker(String value) {
        return clean(value);
    }

    private enum Section {
        NONE,
        SUMMARY,
        ACTIONS,
        CAUTIONS
    }
}
