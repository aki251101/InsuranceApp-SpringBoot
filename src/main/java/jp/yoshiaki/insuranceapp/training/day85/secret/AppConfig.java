package jp.yoshiaki.insuranceapp.training.day85.secret;

/**
 * アプリケーション設定を一元管理するクラス。
 *
 * 実務ではSpringの@Value("${...}")で注入するが、
 * 今回は「環境変数を読み取る仕組み」を学ぶため、
 * System.getenv() で環境変数から値を取得する。
 *
 * ポイント：
 * - APIキーなどの秘密情報はソースコードに直書きしない
 * - 環境変数が未設定の場合はデフォルト値（またはnull）を使う
 * - getMaskedApiKey() でログや画面に安全に表示できる
 */
public class AppConfig {

    // ① 環境変数名を定数で管理（タイポ防止＋変更時に1箇所で済む）
    private static final String ENV_API_KEY = "NOTIFICATION_API_KEY";
    private static final String ENV_ENDPOINT = "NOTIFICATION_ENDPOINT";

    // ② デフォルト値（環境変数が未設定の場合に使う）
    private static final String DEFAULT_ENDPOINT = "https://api.example.com/notify";

    // ③ 設定値を保持するフィールド
    private final String apiKey;
    private final String endpoint;

    /**
     * コンストラクタ：環境変数から設定値を読み込む。
     *
     * System.getenv(name) は、OSに設定された環境変数の値を返す。
     * 未設定の場合は null を返す（例外は発生しない）。
     */
    public AppConfig() {
        // ④ 環境変数からAPIキーを取得（未設定ならnull）
        this.apiKey = System.getenv(ENV_API_KEY);

        // ⑤ 環境変数からエンドポイントを取得（未設定ならデフォルト値）
        String envEndpoint = System.getenv(ENV_ENDPOINT);
        if (envEndpoint != null && !envEndpoint.isBlank()) {
            this.endpoint = envEndpoint;
        } else {
            this.endpoint = DEFAULT_ENDPOINT;
        }
    }

    /**
     * APIキーを取得する。
     *
     * @return APIキー（未設定の場合はnull）
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * 通知エンドポイントURLを取得する。
     *
     * @return エンドポイントURL
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * APIキーが設定済みかどうかを判定する。
     *
     * @return true=設定済み / false=未設定またはブランク
     */
    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * APIキーをマスクして安全に表示できる形にする。
     *
     * 例："sk-test-abc123" → "sk-t****23"
     * ログや画面にAPIキーを表示する際、全文を出さずに
     * 先頭4文字＋****＋末尾2文字にする。
     *
     * @return マスク済みAPIキー（未設定の場合は"（未設定）"）
     */
    public String getMaskedApiKey() {
        if (!isApiKeyConfigured()) {
            return "（未設定）";
        }
        // ⑥ 短いキーの場合はそのままマスクすると情報漏洩するため全マスク
        if (apiKey.length() <= 6) {
            return "****";
        }
        // ⑦ 先頭4文字 + **** + 末尾2文字
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 2);
    }

    /**
     * 環境変数名（APIキー用）を取得する。
     * エラーメッセージやヘルプ表示で使用。
     *
     * @return 環境変数名の文字列
     */
    public String getApiKeyEnvName() {
        return ENV_API_KEY;
    }
}
