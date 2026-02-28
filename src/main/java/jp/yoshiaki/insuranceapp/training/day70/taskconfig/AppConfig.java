package jp.yoshiaki.insuranceapp.training.day70.taskconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * application.yaml の「app:」セクションをまるごとバインド（結び付け）するクラス。
 *
 * 仕組み：
 *   yml に書いた app.app-name → このクラスの appName フィールドに自動セット
 *   yml に書いた app.max-tasks → このクラスの maxTasks フィールドに自動セット
 *   （ケバブケース app-name ↔ キャメルケース appName は自動変換される）
 *
 * なぜこの方式？：
 *   @Value を何個も書く代わりに、1クラスで設定をまとめて管理できる。
 *   フィールドが増えても、このクラスに追加するだけでOK（散らばらない）。
 */
@Profile("training")
@Component("day70AppConfig")  // ① Bean名を明示（他Dayとの衝突防止）
@ConfigurationProperties(prefix = "app")  // ② yml の「app:」配下をバインド
public class AppConfig {

    // app.app-name に対応（ケバブケース → キャメルケースの自動変換）
    private String appName;

    // app.version に対応
    private String version;

    // app.env に対応
    private String env;

    // app.max-tasks に対応
    private int maxTasks;

    // --- getter / setter（Spring がバインドするために setter が必要） ---

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public int getMaxTasks() {
        return maxTasks;
    }

    public void setMaxTasks(int maxTasks) {
        this.maxTasks = maxTasks;
    }
}
