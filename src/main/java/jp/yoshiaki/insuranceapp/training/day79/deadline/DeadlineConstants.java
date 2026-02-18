package jp.yoshiaki.insuranceapp.training.day79.deadline;

/**
 * 期限計算で使う閾値を定数として集約するクラス。
 * マジックナンバー（意味不明な直書き数値）を避け、
 * 変更時に1か所を修正するだけで済むようにする。
 *
 * 損保業務での対応：
 *   - 早期更改基準 = 満期21日前まで
 *   - 更新可能開始 = 満期2ヶ月前から
 */
public final class DeadlineConstants {

    // ① インスタンス化を禁止（定数クラスなので new させない）
    private DeadlineConstants() {
    }

    // ② 早期更新の基準日数（満期日からこの日数前までに更新すれば「早期更新」）
    public static final int EARLY_RENEWAL_DAYS = 21;

    // ③ 更新可能期間の開始（満期日からこの月数前から更新操作が可能になる）
    public static final int RENEWABLE_MONTHS = 2;

    // ④ 期限接近の基準日数（今日からこの日数以内に満期を迎える契約を「期限接近」とみなす）
    public static final int APPROACHING_DAYS = 21;
}
