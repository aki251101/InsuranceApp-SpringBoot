package jp.yoshiaki.insuranceapp.training.day83.ai;

/**
 * AI要約・リスク分析の業務ロジックを統括するService。
 *
 * 【責務】
 * - PromptBuilderでプロンプトを組み立てる
 * - AiClient（Interface）を通じてAIに問い合わせる
 * - 結果を呼び出し元（Main）に返す
 *
 * 【ポイント】
 * - AiClientはInterfaceで受け取るため、Fake/本番を差し替え可能。
 * - Serviceは「AiClientが何で実装されているか」を知らない（疎結合）。
 */
public class AiSummaryService {

    // AI呼び出し口（Interface型で保持 → Fake/本番を差し替え可能）
    private final AiClient aiClient;

    // プロンプト組み立て専門クラス
    private final PromptBuilder promptBuilder;

    /**
     * コンストラクタでAiClientとPromptBuilderを受け取る（DI：依存性注入）。
     * Mainが「どの実装を使うか」を決めて渡す。
     *
     * @param aiClient AI呼び出しの実装（Fake or 本番）
     * @param promptBuilder プロンプト組み立て担当
     */
    public AiSummaryService(AiClient aiClient, PromptBuilder promptBuilder) {
        this.aiClient = aiClient;
        this.promptBuilder = promptBuilder;
    }

    /**
     * テキストの要約をAIに依頼する。
     *
     * @param text 要約対象のテキスト
     * @return AIが返した要約結果
     * @throws AiClientException AI呼び出し失敗時
     */
    public String summarize(String text) {
        // ① プロンプトを組み立てる
        String prompt = promptBuilder.buildSummarizePrompt(text);
        // ② AIに問い合わせる（Fake or 本番）
        return aiClient.ask(prompt);
    }

    /**
     * テキストのリスク分析をAIに依頼する。
     *
     * @param text 分析対象のテキスト
     * @return AIが返したリスク分析結果
     * @throws AiClientException AI呼び出し失敗時
     */
    public String analyzeRisk(String text) {
        // ① プロンプトを組み立てる
        String prompt = promptBuilder.buildRiskAnalysisPrompt(text);
        // ② AIに問い合わせる
        return aiClient.ask(prompt);
    }

    /**
     * 実際にAIに送るプロンプト文を確認用に返す（AI呼び出しなし）。
     * プロンプトの内容を目視確認したい場合に使う。
     *
     * @param text 対象テキスト
     * @return 要約用のプロンプト文（AIには送らない）
     */
    public String showPrompt(String text) {
        return promptBuilder.buildSummarizePrompt(text);
    }
}
