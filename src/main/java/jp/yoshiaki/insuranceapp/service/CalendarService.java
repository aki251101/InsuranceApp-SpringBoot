package jp.yoshiaki.insuranceapp.service;

import jp.yoshiaki.insuranceapp.client.CalendarClient;
import jp.yoshiaki.insuranceapp.entity.Policy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * カレンダー連携Service（アプリケーション層）
 *
 * 実際の外部連携は CalendarClient 実装に委譲する。
 * - stub       : StubCalendarClient
 * - production : GoogleCalendarClient
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final CalendarClient calendarClient;

    public String createEvent(Policy policy) {
        log.info("カレンダーイベント作成: policyId={}, endDate={}",
                policy.getId(), policy.getEndDate());
        return calendarClient.createEvent(policy);
    }

    public void deleteEvent(String eventId) {
        log.info("カレンダーイベント削除: eventId={}", eventId);
        calendarClient.deleteEvent(eventId);
    }

    public String updateEvent(Policy policy, String eventId) {
        log.info("カレンダーイベント更新: policyId={}, eventId={}",
                policy.getId(), eventId);
        return calendarClient.updateEvent(policy, eventId);
    }
}
