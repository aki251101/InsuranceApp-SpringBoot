package jp.yoshiaki.insuranceapp.service;

import jp.yoshiaki.insuranceapp.entity.Accident;
import jp.yoshiaki.insuranceapp.entity.Policy;
import jp.yoshiaki.insuranceapp.exception.AiApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.base-url}")
    private String baseUrl;

    @Value("${gemini.api.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String summarizePolicy(Policy policy) {
        log.info("契約概要を要約: policyId={}", policy.getId());

        String prompt = String.format(
                "以下の損害保険契約の情報を、保険担当者向けに1〜2文で簡潔に要約してください。\n\n" +
                        "契約番号: %s\n" +
                        "契約者名: %s\n" +
                        "契約開始日: %s\n" +
                        "満期日: %s\n" +
                        "ステータス: %s\n\n" +
                        "要約:",
                policy.getPolicyNumber(),
                policy.getCustomerName(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getEffectiveStatus()
        );

        return callGeminiApi(prompt);
    }

    public String suggestNextActions(Accident accident) {
        log.info("事故の次アクション候補を生成: accidentId={}", accident.getId());

        String prompt = String.format(
                "以下の自動車保険の事故情報から、保険担当者が次に取るべきアクションを3つ、箇条書きで提案してください。\n\n" +
                        "事故受付日: %s\n" +
                        "事故場所: %s\n" +
                        "事故概要: %s\n" +
                        "現在のステータス: %s\n" +
                        "最終対応日: %s\n" +
                        "対応履歴メモ: %s\n\n" +
                        "次のアクション候補（3つ）:",
                accident.getOccurredAt(),
                accident.getPlace() != null ? accident.getPlace() : "不明",
                accident.getDescription() != null ? accident.getDescription() : "未入力",
                accident.getStatusLabel(),
                accident.getLastContactedAt() != null ? accident.getLastContactedAt() : "未対応",
                accident.getMemo() != null && !accident.getMemo().isBlank() ?
                        accident.getMemo() : "なし"
        );

        return callGeminiApi(prompt);
    }

    private String callGeminiApi(String prompt) {
        try {
            String url = String.format("%s/models/%s:generateContent?key=%s",
                    baseUrl, model, apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(
                    Map.of("parts", List.of(
                            Map.of("text", prompt)
                    ))
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractTextFromResponse(response.getBody());
            } else {
                throw new AiApiException("Gemini API からの応答が不正です");
            }

        } catch (Exception e) {
            log.error("Gemini API 呼び出しエラー", e);
            throw new AiApiException("AI応答の取得に失敗しました", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                return "AI応答を取得できませんでした";
            }

            Map<String, Object> candidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) candidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                return "AI応答を取得できませんでした";
            }

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            log.error("Gemini APIレスポンス解析エラー", e);
            return "AI応答の解析に失敗しました";
        }
    }
}
