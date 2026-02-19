package jp.yoshiaki.insuranceapp.training.day80.order;

/**
 * 注文の状態を表すenum。
 * 内部ロジックでは英語名、表示では日本語ラベルを使う。
 */
public enum OrderStatus {

    PENDING("保留中"),
    CONFIRMED("確定"),
    FAILED("失敗");

    private final String japanese;

    OrderStatus(String japanese) {
        this.japanese = japanese;
    }

    /** 表示用の日本語ラベルを返す */
    public String toJapanese() {
        return japanese;
    }
}
