package jp.yoshiaki.insuranceapp.client;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import jp.yoshiaki.insuranceapp.config.GoogleCalendarConfig;
import jp.yoshiaki.insuranceapp.entity.Policy;
import jp.yoshiaki.insuranceapp.exception.CalendarApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

/**
 * CalendarClient の本番実装（Google Calendar API）
 *
 * profile=production のときだけ有効。
 * Google Calendar API を使って、満期日イベントの作成・削除・更新を行う。
 *
 * 完成版コード（insurance-app-java/java-source/service/CalendarService.java）に準拠。
 * 主な違い：CalendarClient interface を implements している点のみ。
 */
@Component
@Profile("production")  // ① 本番環境（profile=production）でのみ有効
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarClient implements CalendarClient {

    private final GoogleCalendarConfig googleCalendarConfig;

    // ② "primary" = ログイン中ユーザーのメインカレンダー
    private static final String CALENDAR_ID = "primary";

    /**
     * Googleカレンダーにイベントを作成する
     *
     * イベント内容：
     *   - タイトル：「【満期】○○ 様」
     *   - 説明：契約番号、契約者名、満期日
     *   - 日付：満期日（終日イベント）
     *   - リマインダー：7日前の9:00にポップアップ通知
     *
     * @param policy 契約エンティティ
     * @return GoogleカレンダーのイベントID
     * @throws CalendarApiException API呼び出し失敗時
     */
    @Override
    public String createEvent(Policy policy) {
        log.info("カレンダーイベント作成: policyId={}, endDate={}",
                policy.getId(), policy.getEndDate());

        try {
            // ③ OAuth認証済みのCalendarサービスオブジェクトを取得
            Calendar service = googleCalendarConfig.getCalendarService();

            // ④ イベントオブジェクトを組み立て
            Event event = new Event()
                    .setSummary("【満期】" + policy.getCustomerName() + " 様")
                    .setDescription(
                            "契約番号: " + policy.getPolicyNumber() + "\n" +
                            "契約者名: " + policy.getCustomerName() + "\n" +
                            "満期日: " + policy.getEndDate() + "\n\n" +
                            "※ 早めの更新手続きをお願いします。"
                    );

            // ⑤ 終日イベントとして設定（setDate を使う。setDateTime ではない）
            LocalDate endDate = policy.getEndDate();
            EventDateTime start = new EventDateTime()
                    .setDate(new DateTime(Date.from(
                            endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));
            event.setStart(start);

            // ⑥ 終日イベントの「終了」は翌日を指定（Googleカレンダーの仕様）
            EventDateTime end = new EventDateTime()
                    .setDate(new DateTime(Date.from(
                            endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())));
            event.setEnd(end);

            // ⑦ リマインダー：7日前 = 7 × 24 × 60 = 10080分前にポップアップ通知
            EventReminder[] reminders = new EventReminder[]{
                    new EventReminder()
                            .setMethod("popup")
                            .setMinutes(7 * 24 * 60)  // 10080分 = 7日前
            };
            Event.Reminders eventReminders = new Event.Reminders()
                    .setUseDefault(false)         // デフォルト通知を無効化
                    .setOverrides(Arrays.asList(reminders));  // カスタム通知を設定
            event.setReminders(eventReminders);

            // ⑧ Google Calendar API にイベントを登録
            event = service.events().insert(CALENDAR_ID, event).execute();

            log.info("カレンダーイベント作成成功: eventId={}", event.getId());
            return event.getId();

        } catch (IOException | GeneralSecurityException e) {
            log.error("カレンダーイベント作成失敗: policyId={}", policy.getId(), e);
            throw new CalendarApiException("カレンダーイベントの作成に失敗しました", e);
        }
    }

    /**
     * Googleカレンダーからイベントを削除する
     *
     * @param eventId 削除対象のイベントID
     * @throws CalendarApiException API呼び出し失敗時
     */
    @Override
    public void deleteEvent(String eventId) {
        log.info("カレンダーイベント削除: eventId={}", eventId);

        if (eventId == null || eventId.isBlank()) {
            log.warn("イベントIDが空のため削除をスキップ");
            return;
        }

        try {
            Calendar service = googleCalendarConfig.getCalendarService();
            service.events().delete(CALENDAR_ID, eventId).execute();
            log.info("カレンダーイベント削除成功: eventId={}", eventId);

        } catch (IOException | GeneralSecurityException e) {
            log.error("カレンダーイベント削除失敗: eventId={}", eventId, e);
            throw new CalendarApiException("カレンダーイベントの削除に失敗しました", e);
        }
    }

    /**
     * Googleカレンダーのイベントを更新する（削除＋再作成）
     *
     * Google Calendar APIのupdate()ではなく、delete+createで実装。
     * 理由：終日イベントの日付変更は部分更新が複雑になるため、
     *       削除＋再作成のほうがシンプルで確実。
     *
     * @param policy 契約エンティティ（更新後の情報）
     * @param eventId 既存のイベントID
     * @return 新しいイベントID
     */
    @Override
    public String updateEvent(Policy policy, String eventId) {
        log.info("カレンダーイベント更新: policyId={}, eventId={}",
                policy.getId(), eventId);

        deleteEvent(eventId);
        return createEvent(policy);
    }
}
