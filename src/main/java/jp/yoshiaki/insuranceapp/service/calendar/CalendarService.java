package jp.yoshiaki.insuranceapp.service.calendar;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import jp.yoshiaki.insuranceapp.config.GoogleCalendarConfig;
import jp.yoshiaki.insuranceapp.domain.policy.Policy;
import jp.yoshiaki.insuranceapp.exception.CalendarApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

/**
 * Googleカレンダー連携Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final GoogleCalendarConfig googleCalendarConfig;
    private static final String CALENDAR_ID = "primary";

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

            LocalDate endDate = policy.getEndDate();
            EventDateTime start = new EventDateTime()
                    .setDate(new DateTime(Date.from(
                            endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));
            event.setStart(start);

            EventDateTime end = new EventDateTime()
                    .setDate(new DateTime(Date.from(
                            endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())));
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

        } catch (IOException | GeneralSecurityException e) {
            log.error("カレンダーイベント作成失敗: policyId={}", policy.getId(), e);
            throw new CalendarApiException("カレンダーイベントの作成に失敗しました", e);
        }
    }

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

    public String updateEvent(Policy policy, String eventId) {
        log.info("カレンダーイベント更新: policyId={}, eventId={}", 
                policy.getId(), eventId);

        deleteEvent(eventId);
        return createEvent(policy);
    }
}
