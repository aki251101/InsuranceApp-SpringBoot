package jp.yoshiaki.insuranceapp.training.day67.book;

/**
 * 「見つからない」を表す業務例外
 * 
 * 【設計意図】
 * - 存在しないIDでのアクセス時に投げる
 * - GlobalExceptionHandlerで404 Not Foundに変換される
 * - RuntimeException継承なので、throws宣言が不要（非検査例外）
 */
public class NotFoundException extends RuntimeException {

    /**
     * メッセージ付きコンストラクタ
     * 
     * @param message エラーメッセージ（例：「書籍が見つかりません: id=999」）
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * メッセージ＋原因付きコンストラクタ
     * 
     * @param message エラーメッセージ
     * @param cause 原因となった例外
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
