package jp.yoshiaki.insuranceapp.client.stub;

import jp.yoshiaki.insuranceapp.client.AiClient;
import jp.yoshiaki.insuranceapp.domain.accident.Accident;
import jp.yoshiaki.insuranceapp.domain.policy.Policy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * AI連携のStub実装（開発・テスト用）
 *
 * 本物の Gemini API を呼ばず、固定テンプレートの文字列を返す。
 * spring.profiles.active=stub のときだけ Bean として登録される。
 *
 * 目的:
 *   - APIキーが無くてもアプリ全体が起動する
 *   - 画面操作 → Controller → AiClient の動線が通ることを確認できる
 *   - 本番実装（GeminiAiClient）と差し替えるだけで本物に繋がる
 */
@Component
@Profile("stub")  // ① "stub" プロファイルが有効なときだけ Bean 登録される
@Slf4j
public class StubAiClient implements AiClient {

    /**
     * 契約要約のStub実装
     *
     * 契約情報を埋め込んだ固定テンプレートを返す。
     * 実際のAI APIは呼ばない。
     *
     * @param policy 対象の契約
     * @return Stubが生成した固定の要約テキスト
     */
    @Override
    public String summarizePolicy(Policy policy) {
        log.info("[Stub] 契約要約生成: policyId={}", policy.getId());

        // ② 固定テンプレートだが、契約情報を埋め込んで「それらしい応答」にする
        //    → 画面表示の確認がしやすくなる
        return String.format(
                "[Stub応答] 契約番号 %s（%s 様）は %s 満期の契約です。" +
                "現在のステータスは「%s」です。" +
                "※これはStub（ダミー）応答です。本番ではAIが要約を生成します。",
                policy.getPolicyNumber(),
                policy.getCustomerName(),
                policy.getEndDate(),
                policy.getEffectiveStatus()
        );
    }

    /**
     * 次アクション候補のStub実装
     *
     * 事故情報を埋め込んだ固定テンプレートを返す。
     * 実際のAI APIは呼ばない。
     *
     * @param accident 対象の事故
     * @return Stubが生成した固定の次アクション候補テキスト
     */
    @Override
    public String suggestNextActions(Accident accident) {
        log.info("[Stub] 次アクション候補生成: accidentId={}", accident.getId());

        // ③ 箇条書き形式のテンプレート（画面での表示確認がしやすい形）
        return String.format(
                "[Stub応答] 事故ID: %d（ステータス: %s）に対する次アクション候補:\n" +
                "1. 契約者への状況確認連絡を行う\n" +
                "2. 現場の写真・書類を収集する\n" +
                "3. 関係各所（修理工場・病院等）への連絡を手配する\n" +
                "※これはStub（ダミー）応答です。本番ではAIが状況に応じた提案を生成します。",
                accident.getId(),
                accident.getStatusLabel()
        );
    }
}
