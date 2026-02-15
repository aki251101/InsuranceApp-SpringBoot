package jp.yoshiaki.insuranceapp.training.day76.oauth;

/**
 * OAuthで取得したトークン一式を保持するドメインクラス。
 * アクセストークン（短命）とリフレッシュトークン（長命）を持つ。
 */
public class TokenInfo {

    private final String accessToken;   // APIを呼ぶための短命な許可証
    private final String refreshToken;  // アクセストークンを再発行するための長命なトークン
    private final long expiresAt;       // アクセストークンの有効期限（エポックミリ秒）
    private final String scope;         // 許可された操作の範囲

    // ① コンストラクタ：全フィールドを受け取る
    public TokenInfo(String accessToken, String refreshToken, long expiresAt, String scope) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.scope = scope;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public String getScope() {
        return scope;
    }

    // ② 有効期限切れかどうかを判定する
    //    現在時刻がexpiresAtを超えていたら期限切れ（true）
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    // ③ 残り秒数を計算して返す（表示用）
    public long remainingSeconds() {
        long remaining = (expiresAt - System.currentTimeMillis()) / 1000;
        return Math.max(remaining, 0);
    }
}
