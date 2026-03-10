package jp.yoshiaki.insuranceapp.training.day99.smoketest;

import org.springframework.stereotype.Component;

/**
 * 外部APIスタブ（External Service Stub）
 *
 * 実際の外部API（Gemini AI / Google Calendar 等）の代わりに、
 * 疎通確認用の ping() メソッドを提供する。
 *
 * FaultSimulatorの外部API障害フラグがONの場合、例外を投げる。
 * これにより「外部APIが落ちたときにアプリがどう振る舞うか」を体験する。
 */
@Component("day99ExternalServiceStub")
public class ExternalServiceStub {

    private final FaultSimulator faultSimulator;

    public ExternalServiceStub(FaultSimulator faultSimulator) {
        this.faultSimulator = faultSimulator;
    }

    /**
     * 外部APIへの疎通確認（ping）
     * 正常時：trueを返す
     * 障害時：RuntimeExceptionを投げる
     *
     * @return true（正常時）
     * @throws RuntimeException 外部API障害シミュレーション時
     */
    public boolean ping() {
        if (faultSimulator.isExternalFault()) {
            throw new RuntimeException(
                    "外部API接続失敗（シミュレーション）: 外部サービスが応答しません（タイムアウト）");
        }
        return true;
    }
}
