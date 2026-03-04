package jp.yoshiaki.insuranceapp.service.calendar;

import jp.yoshiaki.insuranceapp.domain.policy.Policy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * CalendarClient の Stub（スタブ）実装
 *
 * 開発環境（profile=stub）で有効。
 * Google Calendar API を呼ばず、ログ出力だけ行う。
 * 戻り値は "stub-event-" + UUID前8桁のダミーID。
 *
 * 使い分け：
 *   - Stub：何もしない／固定値を返す（＝今回のこれ）
 *   - Fake：簡易的に動作する偽物（例：InMemory に保存して一覧が取れる）
 *   - Mock：テスト用。呼ばれた回数や引数を検証できる（Mockitoなど）
 */
@Component
@Profile("stub")  // ① application.yml の spring.profiles.active=stub のときだけ有効
@Slf4j
public class StubCalendarClient implements CalendarClient {

    /**
     * イベント作成（Stub版：ログ出力＋ダミーID返却）
     *
     * 本番では Google Calendar API にイベントが作られるが、
     * Stubではログに記録するだけ。動作確認には十分。
     */
    @Override
    public String createEvent(Policy policy) {
        // ② ダミーのイベントIDを生成（UUID前8桁で十分に一意）
        String dummyEventId = "stub-event-" + UUID.randomUUID().toString().substring(0, 8);

        log.info("[STUB] カレンダーイベント作成: policyId={}, customerName={}, endDate={}, dummyEventId={}",
                policy.getId(),
                policy.getCustomerName(),
                policy.getEndDate(),
                dummyEventId);

        return dummyEventId;
    }

    /**
     * イベント削除（Stub版：ログ出力のみ）
     */
    @Override
    public void deleteEvent(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            log.warn("[STUB] イベントIDが空のため削除をスキップ");
            return;
        }

        log.info("[STUB] カレンダーイベント削除: eventId={}", eventId);
    }

    /**
     * イベント更新（Stub版：削除＋再作成のログ出力）
     */
    @Override
    public String updateEvent(Policy policy, String eventId) {
        log.info("[STUB] カレンダーイベント更新: policyId={}, oldEventId={}",
                policy.getId(), eventId);

        deleteEvent(eventId);
        return createEvent(policy);
    }
}
