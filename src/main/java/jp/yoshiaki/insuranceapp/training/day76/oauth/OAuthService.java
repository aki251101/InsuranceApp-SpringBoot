package jp.yoshiaki.insuranceapp.training.day76.oauth;

/**
 * OAuth 2.0の認可フロー全体を管理するサービスクラス。
 *
 * 実務での対応：
 * - Spring SecurityのOAuth2 Login機能がこの役割を自動で担う
 * - application.ymlにclient-id/client-secretを書くだけで認可フローが動く
 * - このクラスでは「中で何が起きているか」を可視化するために手動で書いている
 */
public class OAuthService {

    private final FakeAuthServer authServer; // 認可サーバー（模擬）
    private final TokenStore tokenStore;     // トークン保管場所

    // ① コンストラクタ：依存を外から受け取る（DI）
    public OAuthService(FakeAuthServer authServer, TokenStore tokenStore) {
        this.authServer = authServer;
        this.tokenStore = tokenStore;
    }

    /**
     * ② 認可フローを実行する。
     *    1. 認可コードを取得（ユーザーが「許可」を押すシミュレーション）
     *    2. 認可コードをアクセストークンに交換
     *    3. トークンをTokenStoreに保存
     */
    public void authorize() {
        System.out.println("【OAuth 2.0 認可コードフロー開始】");
        System.out.println();
        System.out.println("--- ステップ1: 認可リクエスト ---");
        System.out.println("  アプリ → Google認可サーバー: 「カレンダーの書き込み権限をください」");

        // ステップ1: 認可コードを取得
        String scope = "calendar.events";
        String authCode = authServer.issueAuthorizationCode(scope);

        System.out.println();
        System.out.println("--- ステップ2: トークン交換 ---");
        System.out.println("  アプリ → Googleトークンエンドポイント: 認可コード「" + authCode + "」を送信");

        // ステップ2: 認可コードをトークンに交換
        TokenInfo tokenInfo = authServer.exchangeToken(authCode);

        // ステップ3: トークンを保存
        tokenStore.save(tokenInfo);

        System.out.println();
        System.out.println("--- ステップ3: トークン保存完了 ---");
        System.out.println("  認証成功！カレンダーへの操作が可能になりました。");
        System.out.println("  ※ アクセストークンは" + tokenInfo.remainingSeconds() + "秒後に期限切れになります。");
    }

    /**
     * ③ 現在のトークン状態を表示用に返す。
     */
    public String showTokenStatus() {
        if (!tokenStore.isAuthenticated()) {
            return "状態: 未認証（authコマンドで認証してください）";
        }

        TokenInfo info = tokenStore.getTokenInfo();
        StringBuilder sb = new StringBuilder();
        sb.append("状態: 認証済み\n");
        sb.append("  アクセストークン: ").append(info.getAccessToken()).append("\n");
        sb.append("  スコープ: ").append(info.getScope()).append("\n");

        if (info.isExpired()) {
            sb.append("  ⚠ トークン期限切れ（再認証が必要です）");
        } else {
            sb.append("  残り有効時間: ").append(info.remainingSeconds()).append("秒");
        }

        return sb.toString();
    }

    /**
     * ④ トークンを無効化する（認可の取り消し）。
     *    ユーザーが「このアプリの連携を解除したい」ときに使う。
     */
    public void revoke() {
        if (!tokenStore.isAuthenticated()) {
            System.out.println("  すでに未認証の状態です。");
            return;
        }
        tokenStore.clear();
        System.out.println("  トークンを無効化しました。カレンダーへの操作には再認証が必要です。");
    }
}
