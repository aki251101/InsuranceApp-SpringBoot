package jp.yoshiaki.insuranceapp.training.day85.secret;

/**
 * 秘密情報（APIキー等）が未設定の場合にスローされる業務例外。
 * 設定漏れを「原因が追える形」で通知する。
 */
public class SecretNotConfiguredException extends RuntimeException {

    // ① どの秘密情報が未設定かを保持するフィールド
    private final String secretName;

    /**
     * コンストラクタ：未設定の秘密情報名を受け取り、メッセージを組み立てる。
     *
     * @param secretName 未設定の秘密情報の名前（例："NOTIFICATION_API_KEY"）
     */
    public SecretNotConfiguredException(String secretName) {
        // ② 親クラス（RuntimeException）に「原因が追えるメッセージ」を渡す
        super("秘密情報が未設定です: " + secretName
                + "（環境変数またはapplication.ymlで設定してください）");
        this.secretName = secretName;
    }

    /**
     * 未設定の秘密情報名を取得する。
     *
     * @return 秘密情報の名前
     */
    public String getSecretName() {
        return secretName;
    }
}
