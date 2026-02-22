package jp.yoshiaki.insuranceapp.training.day83.ai;

/**
 * AI呼び出し失敗時の業務例外。
 * 本番ではHTTPタイムアウト・レート制限・APIキー不正などが原因となる。
 * Fakeでは意図的にスローして、呼び出し元の例外処理を検証する。
 */
public class AiClientException extends RuntimeException {

    // ① メッセージ付きコンストラクタ（エラー内容を伝える）
    public AiClientException(String message) {
        super(message);
    }

    // ② 原因例外を連鎖させるコンストラクタ（本番で元例外を包むとき用）
    public AiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
