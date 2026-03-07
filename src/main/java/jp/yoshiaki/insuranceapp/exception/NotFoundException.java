package jp.yoshiaki.insuranceapp.exception;

/**
 * リソース未検出例外（NotFoundException）
 *
 * DB検索で対象が見つからない場合に throw する。
 * GlobalExceptionHandler がキャッチし、HTTP 404 ステータスを返す。
 *
 * 使い分け：
 *   - NotFoundException → 「対象が存在しない」（404相当）
 *   - BusinessException → 「対象は存在するが、業務ルール上NGである」（400相当）
 */
public class NotFoundException extends RuntimeException {

    /**
     * コンストラクタ（メッセージのみ）
     *
     * @param message エラーメッセージ（例："契約が見つかりません: id=999"）
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * コンストラクタ（メッセージ＋原因例外）
     *
     * @param message エラーメッセージ
     * @param cause   原因例外
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
