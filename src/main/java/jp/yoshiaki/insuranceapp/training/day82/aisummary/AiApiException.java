package jp.yoshiaki.insuranceapp.training.day82.aisummary;

/**
 * AI API呼び出しの失敗を表す業務例外。
 *
 * RuntimeExceptionを継承しているため、呼び出し元でcatchを強制しない（非検査例外）。
 * ただし、Serviceやコントローラーで適切にハンドリングすることを推奨。
 */
public class AiApiException extends RuntimeException {

    // ① メッセージのみ（原因が特定できないケース）
    public AiApiException(String message) {
        super(message);
    }

    // ② メッセージ＋原因（APIの通信エラー等、元の例外を保持するケース）
    public AiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
