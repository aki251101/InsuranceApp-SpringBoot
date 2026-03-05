package jp.yoshiaki.insuranceapp.service;

import jp.yoshiaki.insuranceapp.client.CalendarClient;
import jp.yoshiaki.insuranceapp.entity.Policy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * カレンダー連携Service（アプリ内の窓口）
 *
 * 目的:
 *  - Service層（PolicyService 等）は GoogleCalendarConfig / Google API を直接参照しない
 *  - 実際の外部連携は CalendarClient に委譲し、stub / production をプロファイルで差し替える
 *
 * 実装クラス例:
 *  - stub       : jp.yoshiaki.insuranceapp.client.stub.StubCalendarClient
 *  - production : （例）jp.yoshiaki.insuranceapp.client.google.GoogleCalendarClient など
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final CalendarClient calendarClient;

    /**
     * 満期日リマインドイベントをカレンダーに登録する
     *
     * @param policy 対象の契約
     * @return 作成されたイベントID（stubの場合は固定のイベントID）
     */
    public String createEvent(Policy policy) {
        log.info("カレンダーイベント作成（委譲）: policyId={}, endDate={}",
                policy.getId(), policy.getEndDate());

        String eventId = calendarClient.createEvent(policy);

        log.info("カレンダーイベント作成完了（委譲）: policyId={}, eventId={}",
                policy.getId(), eventId);
        return eventId;
    }

    /**
     * カレンダーからイベントを削除する
     *
     * @param eventId 削除対象のイベントID
     */
    public void deleteEvent(String eventId) {
        log.info("カレンダーイベント削除（委譲）: eventId={}", eventId);

        // 安全ガード（Stub実装側も安全だが二重で守る）
        if (eventId == null || eventId.isBlank()) {
            log.warn("イベントIDが空のため削除をスキップ");
            return;
        }

        calendarClient.deleteEvent(eventId);

        log.info("カレンダーイベント削除完了（委譲）: eventId={}", eventId);
    }

    /**
     * カレンダーイベントを更新する（削除→再作成）
     *
     * @param policy 対象の契約
     * @param eventId 既存イベントID
     * @return 新しく作成されたイベントID
     */
    public String updateEvent(Policy policy, String eventId) {
        log.info("カレンダーイベント更新（委譲）: policyId={}, eventId={}",
                policy.getId(), eventId);

        deleteEvent(eventId);
        return createEvent(policy);
    }
}
