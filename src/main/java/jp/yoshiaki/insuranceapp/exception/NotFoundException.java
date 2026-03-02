// 配置：src/main/java/jp/yoshiaki/insuranceapp/exception/NotFoundException.java
package jp.yoshiaki.insuranceapp.exception;

/**
 * リソースが見つからない場合にスローする業務例外。
 *
 * 使い方：
 *   Service で findById() の結果が empty だったときに throw する。
 *   GlobalExceptionHandler がこの例外をキャッチして HTTP 404 に変換する。
 *
 * たとえ話：
 *   図書館で「この本ありますか？」と聞いて「該当なし」と返ってきた状態。
 *   プログラムに「見つからなかった」ことを明確に伝えるための専用クラス。
 */
public class NotFoundException extends RuntimeException {

    /**
     * メッセージのみ指定するコンストラクタ。
     *
     * @param message エラーメッセージ（例："契約が見つかりません: id=999"）
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * メッセージ + 原因例外を指定するコンストラクタ。
     *
     * @param message エラーメッセージ
     * @param cause   原因となった元の例外
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
