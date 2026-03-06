package jp.yoshiaki.insuranceapp.client;

/**
 * AI連携クライアント（境界 interface）
 *
 * 外部AIサービス（Gemini API 等）との接続口を定義する。
 * Service/Controller層はこの interface だけを参照し、裏が Stub か本番かを意識しない。
 *
 * 実装クラス:
 *   - StubAiClient   : 開発/テスト用（固定文言を返す）
 *   - GeminiAiClient : 本番用（Gemini APIを呼び出す）
 */
public interface AiClient {

    /**
     * AIにプロンプトを送り、応答テキストを受け取る。
     *
     * @param prompt AIへの指示文
     * @return AI応答テキスト
     */
    String ask(String prompt);
}
