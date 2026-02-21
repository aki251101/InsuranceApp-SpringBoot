package jp.yoshiaki.insuranceapp.training.day82.aisummary;

/**
 * AI APIへの問い合わせ窓口（interface）。
 *
 * 本番では Gemini API を呼ぶ実装に差し替え、
 * 開発・テスト中は FakeAiClient で固定文を返す。
 */
public interface AiClient {

    /**
     * プロンプト（指示文）をAIに送り、応答テキストを返す。
     *
     * @param prompt AIへの指示文
     * @return AIが生成したテキスト
     * @throws AiApiException AI呼び出しに失敗した場合
     */
    String ask(String prompt);
}
