package jp.yoshiaki.insuranceapp.client;

import jp.yoshiaki.insuranceapp.entity.Accident;
import jp.yoshiaki.insuranceapp.entity.Policy;

/**
 * AI連携クライアント（境界 interface）
 *
 * 外部AIサービス（Gemini API 等）との接続口を定義する。
 * Service/Controller層はこの interface だけを参照し、裏が Stub か本番かを意識しない。
 *
 * 実装クラス:
 *   - StubAiClient  : 開発/テスト用（固定テンプレートを返す）
 *   - GeminiAiClient : 本番用（Day95 で実装予定）
 */
public interface AiClient {

    /**
     * 契約情報をAIで要約する
     *
     * @param policy 対象の契約
     * @return AI が生成した要約テキスト
     */
    String summarizePolicy(Policy policy);

    /**
     * 事故情報から次に取るべきアクション候補をAIで生成する
     *
     * @param accident 対象の事故
     * @return AI が生成した次アクション候補テキスト
     */
    String suggestNextActions(Accident accident);
}
