package jp.yoshiaki.insuranceapp.training.day82.aisummary;

/**
 * テスト・開発用のFake実装。
 *
 * 実際のAI APIは呼ばず、プロンプト内容に応じた固定文を返す。
 * 本番では GeminiAiClient 等に差し替える想定。
 */
// @Service("day82FakeAiClient")  ← Spring Boot利用時はこのBean名で登録
public class FakeAiClient implements AiClient {

    // ① プロンプト内容で分岐し、固定の応答を返す
    @Override
    public String ask(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new AiApiException("プロンプトが空です。AIに送る指示文を指定してください。");
        }

        // ② プロンプトに「要約」が含まれていれば要約用の固定文を返す
        if (prompt.contains("要約")) {
            return "【AI要約】この商品は全体的に高評価です。"
                    + "特に品質と使いやすさが好評で、コストパフォーマンスも良いとの声が多数あります。"
                    + "一方、サイズ感については個人差があるようです。";
        }

        // ③ プロンプトに「注意」が含まれていれば注意喚起用の固定文を返す
        if (prompt.contains("注意")) {
            return "【AI注意点】以下の点にご注意ください：\n"
                    + "1. 一部レビューで「初期不良」の報告があります（対応：検品強化の検討）\n"
                    + "2. 配送時の梱包に不満の声があります（対応：梱包方法の見直し）\n"
                    + "3. 説明書がわかりにくいとの指摘があります（対応：FAQ追加の検討）";
        }

        // ④ どちらにも該当しない場合は汎用の応答
        return "【AI応答】ご質問を受け付けました。具体的な分析結果は本番環境でご確認ください。";
    }
}
