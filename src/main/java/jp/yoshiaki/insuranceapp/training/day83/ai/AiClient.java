package jp.yoshiaki.insuranceapp.training.day83.ai;

/**
 * AI APIへの呼び出し口を定義するInterface（境界）。
 *
 * 【なぜInterfaceにするのか？】
 * - 本番（Gemini API）とFake（固定応答）を差し替え可能にするため
 * - Serviceは「AiClientのask()を呼ぶ」という約束だけ知っていればよい
 * - 実装が何であるか（Fake? Gemini? ChatGPT?）を気にしなくてよい
 */
public interface AiClient {

    /**
     * AIにプロンプト（質問文）を送り、回答を受け取る。
     *
     * @param prompt AIに送るプロンプト文（PromptBuilderが組み立てたもの）
     * @return AIの回答テキスト
     * @throws AiClientException AI呼び出しが失敗した場合
     */
    String ask(String prompt);
}
