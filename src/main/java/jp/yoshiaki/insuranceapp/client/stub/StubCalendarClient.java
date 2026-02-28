package jp.yoshiaki.insuranceapp.client.stub;

import jp.yoshiaki.insuranceapp.client.CalendarClient;
import jp.yoshiaki.insuranceapp.domain.Policy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * カレンダー連携のStub実装（開発・テスト用）
 *
 * 本物の Google Calendar API を呼ばず、固定のイベントIDを返す。
 * spring.profiles.active=stub のときだけ Bean として登録される。
 *
 * 目的:
 *   - APIキーが無くてもアプリ全体が起動する
 *   - 画面操作 → Service → CalendarClient の動線が通ることを確認できる
 *   - 本番実装（GoogleCalendarClient）と差し替えるだけで本物に繋がる
 */
@Component
@Profile("stub")  // ① "stub" プロファイルが有効なときだけ Bean 登録される
@Slf4j
public class StubCalendarClient implements CalendarClient {

    /**
     * イベント作成のStub実装
     *
     * 固定の文字列 "stub-event-{policyId}" を返す。
     * 実際のカレンダーには何も登録されない。
     *
     * @param policy 対象の契約
     * @return Stubが生成した固定のイベントID
     */
    @Override
    public String createEvent(Policy policy) {
        // ② ログで「Stubが応答した」ことを明示する（デバッグ時に本番と区別できる）
        String stubEventId = "stub-event-" + policy.getId();
        log.info("[Stub] カレンダーイベント作成: policyId={}, stubEventId={}",
                policy.getId(), stubEventId);
        return stubEventId;
    }

    /**
     * イベント削除のStub実装
     *
     * ログ出力のみ行い、実際には何も削除しない。
     *
     * @param eventId 削除対象のイベントID
     */
    @Override
    public void deleteEvent(String eventId) {
        // ③ 削除もログだけ。eventId が null/空でも安全に動く
        log.info("[Stub] カレンダーイベント削除: eventId={}", eventId);
    }
}
