package jp.yoshiaki.insuranceapp.training.day76.oauth;

import java.util.UUID;

/**
 * Googleの認可サーバーを模擬するクラス（Fake実装）。
 *
 * 本来の流れ：
 *   1. ユーザーがGoogleのログイン画面で許可する
 *   2. Googleが認可コードをアプリに返す（redirect URI経由）
 *   3. アプリが認可コードをGoogleに送り、トークンを受け取る
 *
 * このクラスでは上記をメモリ内でシミュレーションする。
 * アクセストークンの有効期限は60秒に短縮し、期限切れを体験しやすくしている。
 */
public class FakeAuthServer {

    // アクセストークンの有効秒数（本物は3600秒=1時間、学習用に60秒）
    private static final int TOKEN_LIFETIME_SECONDS = 60;

    // 発行済み認可コード（1回限り有効を再現するため保持する）
    private String issuedAuthCode;

    /**
     * ① 認可コードを発行する。
     *    本来はユーザーがGoogleの画面で「許可する」を押した結果として発行される。
     *
     * @param scope 要求するスコープ（例："calendar.events"）
     * @return 認可コード（一時的な引換券）
     */
    public String issueAuthorizationCode(String scope) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("【模擬】Google 認可画面");
        System.out.println("========================================");
        System.out.println("  アプリ「予約管理アプリ」が以下の権限を要求しています：");
        System.out.println("  - スコープ: " + scope);
        System.out.println("  → ユーザーが「許可する」を押しました（シミュレーション）");
        System.out.println("========================================");
        System.out.println();

        // UUIDで一意な認可コードを生成（本物はGoogleが生成する）
        issuedAuthCode = "AUTH-" + UUID.randomUUID().toString().substring(0, 8);
        System.out.println("  認可コード発行: " + issuedAuthCode);
        System.out.println("  （この認可コードをアプリがトークンに交換します）");
        return issuedAuthCode;
    }

    /**
     * ② 認可コードをアクセストークンに交換する。
     *    本来はアプリがGoogleのトークンエンドポイントにPOSTリクエストを送る。
     *
     * @param authCode 認可コード（①で受け取ったもの）
     * @return トークン情報（アクセストークン + リフレッシュトークン + 有効期限）
     * @throws IllegalArgumentException 認可コードが無効な場合
     */
    public TokenInfo exchangeToken(String authCode) {
        // 認可コードの検証（一度使ったら無効にする＝リプレイ攻撃防止）
        if (issuedAuthCode == null || !issuedAuthCode.equals(authCode)) {
            throw new IllegalArgumentException("無効な認可コードです: " + authCode);
        }

        // 使用済みにする（1回限り有効）
        issuedAuthCode = null;

        // トークンを生成
        String accessToken = "AT-" + UUID.randomUUID().toString().substring(0, 12);
        String refreshToken = "RT-" + UUID.randomUUID().toString().substring(0, 12);
        long expiresAt = System.currentTimeMillis() + (TOKEN_LIFETIME_SECONDS * 1000L);

        System.out.println();
        System.out.println("  トークン交換成功！");
        System.out.println("  - アクセストークン: " + accessToken);
        System.out.println("  - リフレッシュトークン: " + refreshToken);
        System.out.println("  - 有効期限: " + TOKEN_LIFETIME_SECONDS + "秒");

        return new TokenInfo(accessToken, refreshToken, expiresAt, "calendar.events");
    }
}
