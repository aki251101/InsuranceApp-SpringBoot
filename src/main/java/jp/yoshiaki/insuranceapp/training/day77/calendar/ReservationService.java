package jp.yoshiaki.insuranceapp.training.day77.calendar;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 予約の業務操作を担当するServiceクラス。
 *
 * CalendarClient（interface）をコンストラクタ注入で受け取る。
 * → 本番用でもFakeでも、同じServiceコードが動く（差し替え可能）。
 *
 * CalendarApiException を catch し、ログ出力＋ユーザー向けメッセージに変換する。
 * → 「外部は失敗する前提」の設計を体現するクラス。
 */
public class ReservationService {

    // ① interface型で保持（具体クラスではなくinterfaceに依存）
    private final CalendarClient calendarClient;

    // ② コンストラクタ注入：外から実装を渡してもらう
    public ReservationService(CalendarClient calendarClient) {
        this.calendarClient = calendarClient;
    }

    /**
     * 予約を登録する。
     * 外部カレンダーAPIの呼び出しが失敗しても、アプリは止まらない。
     *
     * @param title   イベントのタイトル
     * @param dateStr 日付文字列（yyyy-MM-dd形式）
     * @return 結果メッセージ（成功 or 失敗の内容）
     */
    public String createReservation(String title, String dateStr) {
        // ③ 日付のパース（形式が不正なら早期リターン）
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return "エラー：日付の形式が正しくありません（例：2026-03-01）";
        }

        // ④ 外部カレンダーAPIの呼び出し（失敗する前提でtry-catch）
        try {
            String eventId = calendarClient.createEvent(title, date);
            return "予約登録成功：イベントID = " + eventId;
        } catch (CalendarApiException e) {
            // ⑤ 外部API失敗時：ログに記録し、ユーザー向けメッセージを返す
            System.err.println("[ERROR] カレンダー連携失敗: " + e.getMessage());
            return "カレンダー連携に失敗しました（予約は保存されていません）";
        }
    }

    /**
     * 予約を削除する。
     *
     * @param eventId 削除対象のイベントID
     * @return 結果メッセージ
     */
    public String deleteReservation(String eventId) {
        try {
            calendarClient.deleteEvent(eventId);
            return "予約削除成功：イベントID = " + eventId;
        } catch (CalendarApiException e) {
            System.err.println("[ERROR] カレンダー連携失敗: " + e.getMessage());
            return "カレンダー連携に失敗しました（削除できませんでした）";
        }
    }

    /**
     * 予約一覧を取得する。
     *
     * @return 一覧表示用の文字列リスト
     */
    public String listReservations() {
        try {
            List<CalendarEvent> events = calendarClient.listEvents();
            if (events.isEmpty()) {
                return "登録済みの予約はありません";
            }
            StringBuilder sb = new StringBuilder("--- 予約一覧 ---\n");
            for (CalendarEvent event : events) {
                sb.append("  ").append(event.toString()).append("\n");
            }
            return sb.toString().trim();
        } catch (CalendarApiException e) {
            System.err.println("[ERROR] カレンダー連携失敗: " + e.getMessage());
            return "カレンダー連携に失敗しました（一覧を取得できませんでした）";
        }
    }
}
