package jp.yoshiaki.insuranceapp.client;

import jp.yoshiaki.insuranceapp.entity.Policy;

/**
 * カレンダー連携クライアント（境界 interface）
 *
 * 外部カレンダーサービス（Google Calendar 等）との接続口を定義する。
 * Service層はこの interface だけを参照し、裏が Stub か本番かを意識しない。
 *
 * 実装クラス:
 *   - StubCalendarClient  : 開発/テスト用（固定値を返す）
 *   - GoogleCalendarClient : 本番用（Day93 で実装予定）
 */
public interface CalendarClient {

    /**
     * 満期日リマインドイベントをカレンダーに登録する
     *
     * @param policy 対象の契約（満期日・契約者名等を使用）
     * @return 作成されたイベントの ID（カレンダーサービスが返す一意な文字列）
     */
    String createEvent(Policy policy);

    /**
     * カレンダーからイベントを削除する
     *
     * @param eventId 削除対象のイベント ID
     */
    void deleteEvent(String eventId);
}
