package jp.yoshiaki.insuranceapp.exception;

/**
 * AI API呼び出し失敗時の業務例外
 *
 * Gemini APIへの通信失敗、応答の解析失敗など、
 * AI関連の処理で発生するエラーを表す。
 *
 * RuntimeException を継承しているため、
 * メソッドの throws 宣言は不要（非検査例外）。
 */
public class AiApiException extends RuntimeException {

    /**
     * メッセージのみのコンストラクタ
     *
     * @param message エラーメッセージ
     */
    public AiApiException(String message) {
        super(message);
    }

    /**
     * メッセージ＋原因例外のコンストラクタ
     *
     * @param message エラーメッセージ
     * @param cause   原因となった例外（RestTemplateのIOException等）
     */
    public AiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
