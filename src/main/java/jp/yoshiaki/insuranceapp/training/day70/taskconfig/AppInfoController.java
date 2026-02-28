package jp.yoshiaki.insuranceapp.training.day70.taskconfig;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * アプリ情報を返すRESTコントローラ。
 *
 * 設定ファイル（application.yaml + Profile）から読み込んだ値を
 * JSON形式で返す。「設定が正しく読み込まれているか」の確認用エンドポイント。
 */
@Profile("training")
@RestController("day70AppInfoController")  // Bean名を明示（他Dayとの衝突防止）
@RequestMapping("/api/day70")
public class AppInfoController {

    private final AppConfig appConfig;

    // コンストラクタ注入：AppConfigをSpringが自動で渡してくれる
    public AppInfoController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * GET /api/day70/app-info
     * アプリ名・バージョン・環境名・最大タスク数を返す。
     */
    @GetMapping("/app-info")
    public Map<String, Object> getAppInfo() {
        // LinkedHashMapで挿入順を保持（JSONの表示順が安定する）
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("appName", appConfig.getAppName());
        info.put("version", appConfig.getVersion());
        info.put("env", appConfig.getEnv());
        info.put("maxTasks", appConfig.getMaxTasks());
        return info;
    }
}
