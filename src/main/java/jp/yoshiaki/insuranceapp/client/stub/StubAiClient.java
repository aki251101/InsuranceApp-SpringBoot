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
        return "概要:\n"
                + "対応中の事故として、関係者への確認と修理進捗の把握を優先する段階です。\n\n"
                + "対応項目:\n"
                + "・修理工場へ現在の進捗と次回確認予定を確認する\n"
                + "・契約者へ確認済みの状況と今後の連絡予定を伝える\n\n"
                + "注意点:\n"
                + "・対応履歴に残す内容は、日時と確認先が分かる形で記録してください。";
    }
}
