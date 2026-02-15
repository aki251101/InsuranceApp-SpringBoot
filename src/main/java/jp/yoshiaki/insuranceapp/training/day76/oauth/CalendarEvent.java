package jp.yoshiaki.insuranceapp.training.day76.oauth;

/**
 * カレンダーに登録するイベント1件を表現するドメインクラス。
 * 本来はGoogle Calendar APIのEventオブジェクトに相当する。
 */
public class CalendarEvent {

    private final String id;       // イベントID（UUID形式）
    private final String title;    // イベントタイトル
    private final String date;     // イベント日付（YYYY-MM-DD形式）
    private final String createdAt; // 登録日時

    // ① コンストラクタ：全フィールドを受け取って初期化
    public CalendarEvent(String id, String title, String date, String createdAt) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // ② toString：日本語で表示用にフォーマット
    @Override
    public String toString() {
        return String.format("  [%s] %s（日付: %s / 登録日時: %s）", id.substring(0, 8), title, date, createdAt);
    }
}
