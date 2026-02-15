package jp.yoshiaki.insuranceapp.training.day76.oauth;

/**
 * 未認証状態でAPI操作を試みた場合にスローされる業務例外。
 * OAuthトークンが未取得、または無効化済みのときに発生する。
 */
public class UnauthorizedException extends RuntimeException {

    // ① メッセージ付きコンストラクタ
    public UnauthorizedException(String message) {
        super(message);
    }
}
