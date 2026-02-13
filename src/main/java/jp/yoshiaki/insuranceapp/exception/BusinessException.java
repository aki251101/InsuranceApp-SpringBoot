package jp.yoshiaki.insuranceapp.exception;

/**
 * 業務ルール違反時にthrowする例外
 *
 * 使い方：
 *   契約更新ができない状態で更新しようとした場合、
 *   解約済みの契約を操作しようとした場合 など
 *
 * errorCode：エラーの種類を識別するコード（例："POLICY_NOT_RENEWABLE"）
 *   → ログや画面で「何のエラーか」を機械的に判別するために使う
 */
public class BusinessException extends RuntimeException {

    // ① エラーコード（例外の種類を識別するための文字列）
    private final String errorCode;

    // ② コンストラクタ（エラーコード＋メッセージ）
    public BusinessException(String errorCode, String message) {
        super(message);           // 親クラス（RuntimeException）にメッセージを渡す
        this.errorCode = errorCode;
    }

    // ③ コンストラクタ（エラーコード＋メッセージ＋原因例外）
    //    別の例外が原因で業務エラーになった場合に、原因を保持する
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);    // 原因例外（cause）もセットで渡す
        this.errorCode = errorCode;
    }

    // ④ エラーコードの取得
    public String getErrorCode() {
        return errorCode;
    }
}
