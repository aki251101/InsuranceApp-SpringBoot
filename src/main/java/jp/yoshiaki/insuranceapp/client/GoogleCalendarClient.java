package jp.yoshiaki.insuranceapp.client;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
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
import java.util.Arrays;

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
@Profile("production")
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarClient implements CalendarClient {

    private final GoogleCalendarConfig googleCalendarConfig;

    private static final String CALENDAR_ID = "primary";

    @Override
    public String createEvent(Policy policy) {
        log.info("カレンダーイベント作成: policyId={}, endDate={}",
                policy.getId(), policy.getEndDate());

        try {
            Calendar service = googleCalendarConfig.getCalendarService();

            Event event = new Event()
                    .setSummary("【満期】" + policy.getCustomerName() + " 様")
                    .setDescription(
                            "契約番号: " + policy.getPolicyNumber() + "\n" +
                            "契約者名: " + policy.getCustomerName() + "\n" +
                            "満期日: " + policy.getEndDate() + "\n\n" +
                            "※ 早めの更新手続きをお願いします。"
                    );

            // 終日イベントは date-only（yyyy-MM-dd）で送る
            LocalDate endDate = policy.getEndDate();
            EventDateTime start = new EventDateTime()
                    .setDate(new DateTime(endDate.toString()));
            event.setStart(start);

            // Google仕様: 終日は終了日に翌日を指定
            EventDateTime end = new EventDateTime()
                    .setDate(new DateTime(endDate.plusDays(1).toString()));
            event.setEnd(end);

            EventReminder[] reminders = new EventReminder[]{
                    new EventReminder()
                            .setMethod("popup")
                            .setMinutes(7 * 24 * 60)
            };
            Event.Reminders eventReminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminders));
            event.setReminders(eventReminders);

            event = service.events().insert(CALENDAR_ID, event).execute();

            log.info("カレンダーイベント作成成功: eventId={}", event.getId());
            return event.getId();

        } catch (GoogleJsonResponseException e) {
            String details;
            try {
                details = e.getDetails() != null ? e.getDetails().toPrettyString() : e.getMessage();
            } catch (IOException ioEx) {
                details = e.getMessage();
            }
            log.error("カレンダーイベント作成失敗（Google API）: policyId={}, details={}", policy.getId(), details, e);
            throw new CalendarApiException("カレンダーイベントの作成に失敗しました: " + details, e);

        } catch (IOException | GeneralSecurityException e) {
            log.error("カレンダーイベント作成失敗: policyId={}", policy.getId(), e);
            throw new CalendarApiException("カレンダーイベントの作成に失敗しました", e);
        }
    }

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

    @Override
    public String updateEvent(Policy policy, String eventId) {
        log.info("カレンダーイベント更新: policyId={}, eventId={}",
                policy.getId(), eventId);

        deleteEvent(eventId);
        return createEvent(policy);
    }
}

