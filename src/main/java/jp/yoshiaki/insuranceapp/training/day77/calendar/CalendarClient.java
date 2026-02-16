package jp.yoshiaki.insuranceapp.training.day77.calendar;

import java.time.LocalDate;
import java.util.List;

/**
 * 外部カレンダーAPIとの境界（Boundary）を定義するinterface。
 *
 * このinterfaceがあることで：
 * - 本番用（GoogleCalendarClient）とテスト用（FakeCalendarClient）を差し替えられる
 * - Service層は「外部APIの詳細」を知らずに済む（疎結合）
 * - 外部APIが壊れても、Fakeに切り替えて開発・テストを継続できる
 */
public interface CalendarClient {

    /**
     * カレンダーにイベントを作成する。
     *
     * @param title イベントのタイトル（例："会議"）
     * @param date  イベントの日付
     * @return 作成されたイベントのID（例："EVT-0001"）
     * @throws CalendarApiException API呼び出しに失敗した場合
     */
    String createEvent(String title, LocalDate date);

    /**
     * カレンダーからイベントを削除する。
     *
     * @param eventId 削除対象のイベントID
     * @throws CalendarApiException API呼び出しに失敗した場合
     */
    void deleteEvent(String eventId);

    /**
     * 登録済みのイベント一覧を取得する。
     *
     * @return イベントの一覧
     * @throws CalendarApiException API呼び出しに失敗した場合
     */
    List<CalendarEvent> listEvents();
}
