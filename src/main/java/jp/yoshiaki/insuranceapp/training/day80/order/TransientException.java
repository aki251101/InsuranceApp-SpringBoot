package jp.yoshiaki.insuranceapp.training.day80.order;

/**
 * 外部API呼び出し時の一時的な障害を表す例外。
 *
 * 一時的（transient）＝時間が経てば回復する可能性がある障害。
 * この例外が出た場合はリトライする価値がある。
 * （対義語：永続的障害＝認証エラー等。リトライしても無駄）
 */
public class TransientException extends RuntimeException {

    public TransientException(String message) {
        super(message);
    }

    public TransientException(String message, Throwable cause) {
        super(message, cause);
    }
}
