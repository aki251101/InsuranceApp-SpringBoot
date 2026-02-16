package jp.yoshiaki.insuranceapp.training.day77.calendar;

/**
 * カレンダーAPI呼び出し失敗時の業務例外。
 * RuntimeException を継承しているため、呼び出し元で catch を強制されない（非検査例外）。
 * ただし Service 層で意図的に catch してログ＋ユーザー向けメッセージに変換する。
 */
public class CalendarApiException extends RuntimeException {

    // ① メッセージだけ受け取るコンストラクタ
    public CalendarApiException(String message) {
        super(message);
    }

    // ② メッセージ＋原因例外を受け取るコンストラクタ（原因を失わない）
    public CalendarApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
