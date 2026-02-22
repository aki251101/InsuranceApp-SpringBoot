package jp.yoshiaki.insuranceapp.training.day83.ai;

/**
 * プロンプト（AIへの質問文）を組み立てる専門クラス。
 *
 * 【なぜ専用クラスにするのか？】
 * - プロンプトは「業務データ + 指示文」の組み合わせ。
 *   Serviceに直書きすると、プロンプトの改善時にServiceを毎回修正することになる。
 * - PromptBuilderに集約すれば、プロンプトの改善はここだけで完結する。
 * - 将来、テンプレートを外部ファイル（yaml等）から読む拡張も容易になる。
 */
public class PromptBuilder {

    // ① 要約用プロンプトのテンプレート
    private static final String SUMMARIZE_TEMPLATE =
            "以下のテキストを3行以内で要約してください。\n"
            + "専門用語があれば平易な表現に置き換えてください。\n"
            + "\n"
            + "--- 対象テキスト ---\n"
            + "%s\n"
            + "--- ここまで ---";

    // ② リスク分析用プロンプトのテンプレート
    private static final String RISK_ANALYSIS_TEMPLATE =
            "以下のテキストからリスク要因を抽出し、箇条書きで3つまで挙げてください。\n"
            + "各リスクには「影響度（高/中/低）」を付けてください。\n"
            + "\n"
            + "--- 対象テキスト ---\n"
            + "%s\n"
            + "--- ここまで ---";

    /**
     * 要約用のプロンプトを組み立てる。
     *
     * @param text 要約対象のテキスト
     * @return AIに送るプロンプト文
     */
    public String buildSummarizePrompt(String text) {
        // String.format で %s にテキストを埋め込む
        return String.format(SUMMARIZE_TEMPLATE, text);
    }

    /**
     * リスク分析用のプロンプトを組み立てる。
     *
     * @param text 分析対象のテキスト
     * @return AIに送るプロンプト文
     */
    public String buildRiskAnalysisPrompt(String text) {
        return String.format(RISK_ANALYSIS_TEMPLATE, text);
    }
}
