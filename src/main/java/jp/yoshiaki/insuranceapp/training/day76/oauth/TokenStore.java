package jp.yoshiaki.insuranceapp.training.day76.oauth;

/**
 * トークンをインメモリに保管し、取得・削除を提供するクラス。
 * 本来はDBやセッションに保存するが、学習用にメモリで代替する。
 *
 * 実務での対応：
 * - Spring SecurityのOAuth2AuthorizedClientServiceがこの役割を担う
 * - トークンはセッションまたはDBに安全に保管される
 */
public class TokenStore {

    // ① トークン情報（nullなら未認証）
    private TokenInfo tokenInfo;

    // ② トークンを保存する
    public void save(TokenInfo tokenInfo) {
        this.tokenInfo = tokenInfo;
    }

    // ③ トークンを取得する（nullなら未認証）
    public TokenInfo getTokenInfo() {
        return tokenInfo;
    }

    // ④ トークンを削除する（認可の取り消し）
    public void clear() {
        this.tokenInfo = null;
    }

    // ⑤ 認証済みかどうかを判定する
    public boolean isAuthenticated() {
        return tokenInfo != null;
    }
}
