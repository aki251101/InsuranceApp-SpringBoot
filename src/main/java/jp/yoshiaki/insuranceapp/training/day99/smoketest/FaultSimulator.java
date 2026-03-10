package jp.yoshiaki.insuranceapp.training.day99.smoketest;

import org.springframework.stereotype.Component;

/**
 * 障害シミュレーター（Fault Simulator）
 * テスト目的で「DB障害」「外部API障害」を疑似的に発生させるフラグ管理クラス。
 *
 * 実務では本番コードに入れないが、学習・検証用としてスモークテストの挙動を体験するために使う。
 * フラグがONの間、Repository や ExternalServiceStub がわざと例外を投げる。
 */
@Component("day99FaultSimulator")
public class FaultSimulator {

    private boolean dbFault = false;        // DB障害フラグ（デフォルト：正常）
    private boolean externalFault = false;  // 外部API障害フラグ（デフォルト：正常）

    // --- DB障害の制御 ---

    public void enableDbFault() {
        this.dbFault = true;
    }

    public void disableDbFault() {
        this.dbFault = false;
    }

    public boolean isDbFault() {
        return dbFault;
    }

    // --- 外部API障害の制御 ---

    public void enableExternalFault() {
        this.externalFault = true;
    }

    public void disableExternalFault() {
        this.externalFault = false;
    }

    public boolean isExternalFault() {
        return externalFault;
    }

    // --- 全障害リセット ---

    public void resetAll() {
        this.dbFault = false;
        this.externalFault = false;
    }
}
