package jp.yoshiaki.insuranceapp.training.day83.ai;

/**
 * AiClientのFake実装（開発・テスト用）。
 *
 * 【Fakeとは？】
 * - 本番のAI API（Gemini等）の代わりに、固定のレスポンスを返す実装。
 * - API呼び出しなし → 無料・高速・安定（ネットワーク不要）。
 * - プロンプトの構造やServiceの処理フローを検証するのが目的。
 *
 * 【本番との違い】
 * - 本番：HTTPリクエスト → AI推論 → 動的な回答（毎回変わる）
 * - Fake：プロンプトを受け取り → 固定文を返す（毎回同じ）
 * - エラーテスト："__ERROR__" を含むプロンプトで意図的に例外をスロー
 */
public class FakeAiClient implements AiClient {

    // エラーを発生させるためのトリガー文字列
    private static final String ERROR_TRIGGER = "__ERROR__";

    /**
     * プロンプトを受け取り、固定レスポンスを返す。
     * プロンプトに "__ERROR__" が含まれている場合は例外をスローする。
     *
     * @param prompt AIに送るプロンプト文
     * @return 固定のレスポンス文字列
     * @throws AiClientException プロンプトにERROR_TRIGGERが含まれる場合
     */
    @Override
    public String ask(String prompt) {
        // ① エラートリガーチェック（AI障害のシミュレーション）
        if (prompt.contains(ERROR_TRIGGER)) {
            throw new AiClientException(
                    "AI応答エラー: サービスが一時的に利用できません（Fakeによるシミュレーション）"
            );
        }

        // ② プロンプトの内容に応じた固定レスポンスを返す
        if (prompt.contains("要約")) {
            return "[Fake応答] 入力されたテキストの要約です。"
                    + "主要なポイントを3行にまとめました。"
                    + "（※これはFakeの固定レスポンスです。本番ではAIが動的に生成します）";
        }

        if (prompt.contains("リスク")) {
            return "[Fake応答] リスク分析結果:\n"
                    + "・リスク1: 情報不足による判断ミス（影響度: 高）\n"
                    + "・リスク2: 期限超過の可能性（影響度: 中）\n"
                    + "・リスク3: コミュニケーション不足（影響度: 低）\n"
                    + "（※これはFakeの固定レスポンスです）";
        }

        // ③ その他のプロンプトにはデフォルトの応答を返す
        return "[Fake応答] プロンプトを受け取りました。"
                + "（※これはFakeの固定レスポンスです）";
    }
}
