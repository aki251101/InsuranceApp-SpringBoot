package jp.yoshiaki.insuranceapp.service;

import jp.yoshiaki.insuranceapp.entity.Policy;

/**
 * カレンダー外部連携の境界（interface）
 *
 * このinterfaceを挟むことで、本番（Google Calendar API）と
 * 開発（Stub：ログ出力のみ）を @Profile で差し替え可能にする。
 *
 * PolicyService はこのinterfaceだけに依存し、
 * 実装クラスを直接知らない（依存性逆転の原則）。
 */
public interface CalendarClient {

    /**
     * カレンダーにイベントを作成する
     *
     * @param policy 契約エンティティ（満期日・契約者名などを使用）
     * @return イベントID（Googleカレンダー上の識別子。Stubではダミー値）
     */
    String createEvent(Policy policy);

    /**
     * カレンダーからイベントを削除する
     *
     * @param eventId 削除対象のイベントID
     */
    void deleteEvent(String eventId);

    /**
     * カレンダーのイベントを更新する（削除＋再作成で実現）
     *
     * @param policy 契約エンティティ（更新後の情報）
     * @param eventId 既存のイベントID
     * @return 新しいイベントID
     */
    String updateEvent(Policy policy, String eventId);
}
