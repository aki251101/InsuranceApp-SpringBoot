package jp.yoshiaki.insuranceapp.training.day84.monitor;

/**
 * 外部サービス障害を表す業務例外。
 * 外部API固有の例外（ConnectException, SocketTimeoutException等）を
 * この例外にラップ（翻訳）することで、上位レイヤーが外部ライブラリに依存しなくなる。
 */
public class ExternalServiceException extends RuntimeException {

    // どの外部サービスで障害が起きたか（例："WeatherApi"）
    private final String serviceName;

    // 何回試行して失敗したか（リトライ回数の記録）
    private final int attemptCount;

    /**
     * コンストラクタ。
     *
     * @param serviceName  障害元のサービス名
     * @param attemptCount 試行回数
     * @param cause        元の例外（外部ライブラリ固有の例外をラップ）
     */
    public ExternalServiceException(String serviceName, int attemptCount, Throwable cause) {
        // ① superにメッセージとcauseを渡す → スタックトレースで原因を追える
        super(String.format("外部サービス障害: service=%s, attempts=%d", serviceName, attemptCount), cause);
        this.serviceName = serviceName;
        this.attemptCount = attemptCount;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getAttemptCount() {
        return attemptCount;
    }
}
