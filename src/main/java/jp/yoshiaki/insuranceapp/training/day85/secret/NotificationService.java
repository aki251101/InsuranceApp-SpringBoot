package jp.yoshiaki.insuranceapp.training.day85.secret;

/**
 * 通知送信の業務ロジックを担うサービスクラス。
 *
 * 責務：
 * - 送信前にAPIキーが設定されているかを検証する
 * - 検証OKなら NotificationClient に送信を委譲する
 * - 検証NGなら SecretNotConfiguredException をスローする
 *
 * 設計ポイント：
 * - Serviceはinterfaceの NotificationClient に依存する（実装には依存しない）
 * - これにより、Dummy→本番実装への切り替えが容易になる
 */
public class NotificationService {

    // ① 依存オブジェクト（コンストラクタインジェクション：手動DI）
    private final NotificationClient client;
    private final AppConfig config;

    /**
     * コンストラクタ：依存オブジェクトを外部から受け取る。
     *
     * 「自分でnewしない」のがポイント。
     * Springの場合は@Autowiredで自動注入されるが、
     * 今回は学習のため手動で渡す（手動DI）。
     *
     * @param client 通知送信クライアント（interfaceで受け取る）
     * @param config アプリケーション設定
     */
    public NotificationService(NotificationClient client, AppConfig config) {
        this.client = client;
        this.config = config;
    }

    /**
     * 通知メッセージを送信する。
     *
     * 送信前にAPIキーの設定状態を検証し、
     * 未設定の場合は SecretNotConfiguredException をスローする。
     *
     * @param message 送信するメッセージ
     * @throws SecretNotConfiguredException APIキーが未設定の場合
     */
    public void send(String message) {
        // ② 送信前の検証：APIキーが設定されているか
        if (!config.isApiKeyConfigured()) {
            // ③ 原因が追えるメッセージ付きの例外をスロー
            throw new SecretNotConfiguredException(config.getApiKeyEnvName());
        }

        // ④ 検証OK → Clientに送信を委譲
        client.send(config.getEndpoint(), config.getApiKey(), message);
    }
}
