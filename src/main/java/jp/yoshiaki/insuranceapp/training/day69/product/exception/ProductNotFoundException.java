package jp.yoshiaki.insuranceapp.training.day69.product.exception;

/**
 * 指定されたIDの商品が見つからない場合にスローする業務例外。
 * RuntimeExceptionを継承しているため、呼び出し元でcatchを強制しない（非検査例外）。
 */
public class ProductNotFoundException extends RuntimeException {

    // ① 見つからなかったIDを保持（ログやエラーレスポンスで使う）
    private final Long productId;

    public ProductNotFoundException(Long productId) {
        // ② 親クラスのコンストラクタにメッセージを渡す
        super("商品が見つかりません（ID: " + productId + "）");
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}
