package jp.yoshiaki.insuranceapp.client;

import jp.yoshiaki.insuranceapp.exception.AiApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gemini API を使った本番用AIクライアント
 *
 * application.yml の ai.client.type=gemini のときにBeanとして登録される。
 * RestTemplate で Gemini API にHTTP POSTを送り、応答テキストを返す。
 */
@Slf4j
public class GeminiAiClient implements AiClient {

    // ① application.yml から設定値を注入
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.base-url}")
    private String baseUrl;

    @Value("${gemini.api.model}")
    private String model;

    // ② HTTP通信用のユーティリティ（Springが提供する「郵便局の窓口」）
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Gemini API にプロンプトを送り、応答テキストを返す
     *
     * @param prompt AIへの指示文
     * @return AI応答テキスト
     */
    @Override
    public String ask(String prompt) {
        return callGeminiApi(prompt);
    }

    /**
     * Gemini API を実際に呼び出す内部メソッド
     *
     * @param prompt プロンプト
     * @return AI応答テキスト
     * @throws AiApiException 通信失敗・応答不正の場合
     */
    private String callGeminiApi(String prompt) {
        try {
            // ③ APIのURL組み立て（ベースURL + モデル名 + APIキー）
            String url = String.format("%s/models/%s:generateContent?key=%s",
                    baseUrl, model, apiKey);

            // ④ リクエストボディ組み立て（Gemini APIの仕様に合わせたJSON構造）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(
                    Map.of("parts", List.of(
                            Map.of("text", prompt)
                    ))
            ));

            // ⑤ HTTPヘッダー設定（「送るデータはJSONですよ」と伝える）
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // ⑥ ヘッダー + ボディ を「封筒」にまとめる
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // ⑦ POST送信して応答を受け取る
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            // ⑧ 応答からテキスト部分を抽出
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractTextFromResponse(response.getBody());
            } else {
                throw new AiApiException("Gemini API からの応答が不正です");
            }

        } catch (AiApiException e) {
            // AiApiExceptionはそのまま再スロー
            throw e;
        } catch (Exception e) {
            log.error("Gemini API 呼び出しエラー", e);
            throw new AiApiException("AI応答の取得に失敗しました", e);
        }
    }

    /**
     * Gemini APIのJSONレスポンスからテキストを抽出する
     *
     * レスポンス構造:
     * {
     *   "candidates": [{
     *     "content": {
     *       "parts": [{ "text": "AIの応答テキスト" }]
     *     }
     *   }]
     * }
     *
     * @param response APIレスポンスのMap
     * @return 抽出したテキスト
     */
    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            // candidates（候補リスト）を取得
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                return "AI応答を取得できませんでした";
            }

            // 最初の候補 → content → parts → text を辿る
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
