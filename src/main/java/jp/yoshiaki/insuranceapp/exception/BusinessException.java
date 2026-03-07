package jp.yoshiaki.insuranceapp.exception;

/**
 * 業務例外（BusinessException）
 *
 * 業務ルール違反を表すカスタム例外。
 * 例外発生時にエラーコード（errorCode）を持たせることで、
 * GlobalExceptionHandler 側で「どの種類の業務エラーか」を判別できる。
 *
 * RuntimeException を継承しているため、呼び出し側で try-catch を強制しない（非チェック例外）。
 * → Service 層で throw し、Controller 層は @ControllerAdvice が自動でキャッチする設計。
 */
public class BusinessException extends RuntimeException {

    // ① エラーコード（例："POLICY_NOT_RENEWABLE", "ACCIDENT_ALREADY_RESOLVED"）
    private final String errorCode;

    /**
     * コンストラクタ（エラーコード＋メッセージ）
     *
     * @param errorCode エラーコード（例："POLICY_NOT_RENEWABLE"）
     * @param message   エラーメッセージ（例："更新可能期間外のため更新できません"）
     */
    public BusinessException(String errorCode, String message) {
        super(message); // ② RuntimeException の message フィールドにセット
        this.errorCode = errorCode;
    }

    /**
     * コンストラクタ（エラーコード＋メッセージ＋原因例外）
     *
     * @param errorCode エラーコード
     * @param message   エラーメッセージ
     * @param cause     原因例外（例外チェーンで元の原因を保持）
     */
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause); // ③ 原因例外もセット（スタックトレースで追跡可能にする）
        this.errorCode = errorCode;
    }

    /**
     * エラーコードを取得
     *
     * @return エラーコード
     */
    public String getErrorCode() {
        return errorCode;
    }
}
