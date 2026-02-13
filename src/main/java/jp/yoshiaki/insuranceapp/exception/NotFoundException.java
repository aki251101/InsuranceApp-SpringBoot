package jp.yoshiaki.insuranceapp.exception;

/**
 * リソースが見つからない場合にthrowする例外（HTTP 404に対応）
 *
 * 使い方：
 *   指定されたIDの契約がDBに存在しない場合、
 *   指定されたIDの事故報告がDBに存在しない場合 など
 *
 * 例：throw new NotFoundException("契約ID=999が見つかりません");
 */
public class NotFoundException extends RuntimeException {

    // ① コンストラクタ（メッセージのみ）
    public NotFoundException(String message) {
        super(message);
    }

    // ② コンストラクタ（メッセージ＋原因例外）
    //    DB検索時の例外が原因で「見つからない」になった場合等
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
