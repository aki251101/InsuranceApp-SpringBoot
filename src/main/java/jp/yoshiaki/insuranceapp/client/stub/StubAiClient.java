package jp.yoshiaki.insuranceapp.client.stub;

import jp.yoshiaki.insuranceapp.client.AiClient;
import lombok.extern.slf4j.Slf4j;

/**
 * Stub（ダミー）AIクライアント
 *
 * application.yml の ai.client.type=stub のときに Bean として登録される。
 * API Keyが無くてもアプリが動作するよう、固定文言を返す。
 */
@Slf4j
public class StubAiClient implements AiClient {

    /**
     * 固定文言を返すダミー実装
     *
     * @param prompt AIへの指示文（内容は種別判定にのみ利用）
     * @return 固定の応答テキスト
     */
    @Override
    public String ask(String prompt) {
        log.info("[Stub] AIリクエスト受信（固定応答を返します）");
        log.debug("[Stub] prompt={}", prompt);

        if (prompt.contains("要約")) {
            return buildPolicySummaryStub();
        }
        if (prompt.contains("アクション")) {
            return buildAccidentSuggestStub();
        }

        return "[Stub応答] AIからの応答サンプルです。本番環境ではGemini APIが応答します。";
    }

    private String buildPolicySummaryStub() {
        return "現状: 契約中。満期まで30日、更新可、事故履歴1件。\n"
                + "注意点: 満期が近いため更新意向と手続き状況を優先確認。";
    }

    private String buildAccidentSuggestStub() {
        return "[Stub応答] 次のアクション候補：\n"
                + "1. 事故相手への連絡と詳細な状況確認を行う\n"
                + "2. 修理工場への見積依頼と進捗確認を実施する\n"
                + "3. 保険金請求に必要な書類一式を顧客に案内する";
    }
}
