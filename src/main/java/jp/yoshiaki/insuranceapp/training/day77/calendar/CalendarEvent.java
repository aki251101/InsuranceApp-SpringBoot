package jp.yoshiaki.insuranceapp.training.day77.calendar;

import java.time.LocalDate;

/**
 * カレンダーに登録する1件のイベントを表すドメインクラス。
 * 不変（immutable）にするため、フィールドは final＋setter なし。
 */
public class CalendarEvent {

    private final String eventId;   // カレンダー側で採番されるID
    private final String title;     // イベントのタイトル（例："会議"）
    private final LocalDate date;   // イベントの日付

    // ① コンストラクタ：生成時に全フィールドを確定させる
    public CalendarEvent(String eventId, String title, LocalDate date) {
        this.eventId = eventId;
        this.title = title;
        this.date = date;
    }

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDate() {
        return date;
    }

    // ② 表示用：一覧表示で使う文字列を返す
    @Override
    public String toString() {
        return "[" + eventId + "] " + title + "（" + date + "）";
    }
}
