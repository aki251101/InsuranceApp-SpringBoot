package jp.yoshiaki.insuranceapp.training.day78.weather;

/**
 * 外部API呼び出しが失敗した場合にスローする業務例外。
 * 原因（cause）を保持し、ログやエラーレスポンスで「なぜ失敗したか」を追跡できるようにする。
 */
public class ExternalApiException extends RuntimeException {

    // ① メッセージのみ（原因が不明な場合）
    public ExternalApiException(String message) {
        super(message);
    }

    // ② メッセージ＋原因（元の例外を包んで投げる場合）
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
