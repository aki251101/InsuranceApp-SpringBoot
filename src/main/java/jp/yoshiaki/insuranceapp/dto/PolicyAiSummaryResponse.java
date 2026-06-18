package jp.yoshiaki.insuranceapp.dto;

import java.io.Serializable;

/**
 * 契約AI要約の画面表示用DTO。
 */
public record PolicyAiSummaryResponse(
        String current,
        String caution) implements Serializable {

    private static final String CURRENT_HEADING = "現状:";
    private static final String CAUTION_HEADING = "注意点:";

    public static PolicyAiSummaryResponse from(String raw) {
        if (raw == null || raw.isBlank()) {
            return fallback();
        }

        String current = "";
        String caution = "";

        for (String rawLine : raw.lines().toList()) {
            String line = clean(rawLine);
            if (line.isBlank()) {
                continue;
            }

            if (line.startsWith(CURRENT_HEADING)) {
                current = line.substring(CURRENT_HEADING.length()).trim();
                continue;
            }
            if (line.startsWith(CAUTION_HEADING)) {
                caution = line.substring(CAUTION_HEADING.length()).trim();
            }
        }

        if (current.isBlank() && caution.isBlank()) {
            current = clean(raw);
        }
        if (current.isBlank()) {
            current = "契約情報に基づく要約です。";
        }
        if (caution.isBlank()) {
            caution = "特記事項なし";
        }

        return new PolicyAiSummaryResponse(current, caution);
    }

    private static PolicyAiSummaryResponse fallback() {
        return new PolicyAiSummaryResponse("契約情報に基づく要約です。", "特記事項なし");
    }

    private static String clean(String value) {
        return value == null ? "" : value
                .trim()
                .replace("**", "")
                .replaceAll("^\\s*[・*\\-]\\s*", "")
                .trim();
    }
}
