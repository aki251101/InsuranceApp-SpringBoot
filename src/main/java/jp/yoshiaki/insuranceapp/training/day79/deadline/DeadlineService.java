package jp.yoshiaki.insuranceapp.training.day79.deadline;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 期限計算のロジックを集約するServiceクラス。
 * 「満期日の何日前か」「早期更新か通常更新か」などの判定はすべてここで行う。
 *
 * 損保アプリでの対応：
 *   - RenewalStatsService の isEarlyRenewal() メソッド
 *   - Policy の isRenewable() / isAttentionRequired() メソッド
 *   - DateUtil の年度計算メソッド
 *
 * これらの「日付を基準にした業務判定」を1クラスにまとめた学習版。
 */
public class DeadlineService {

    private final InMemorySubscriptionRepository repository;

    // ① コンストラクタインジェクション（Repositoryを外から受け取る）
    public DeadlineService(InMemorySubscriptionRepository repository) {
        this.repository = repository;
    }

    // ② 契約を登録する
    public Subscription register(String name, LocalDate startDate, LocalDate endDate) {
        // 開始日が終了日より後ならエラー
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "開始日が終了日より後になっています: " + startDate + " > " + endDate);
        }
        long id = repository.nextId();
        Subscription subscription = new Subscription(id, name, startDate, endDate);
        return repository.save(subscription);
    }

    // ③ 期限状態をチェックして文字列で返す
    public String checkDeadline(long id) {
        Subscription sub = findByIdOrThrow(id);
        return buildDeadlineStatus(sub, LocalDate.now());
    }

    // ④ 契約を更新し、「早期更新 or 通常更新」を判定して返す
    public String renew(long id) {
        Subscription sub = findByIdOrThrow(id);
        LocalDate today = LocalDate.now();

        // 更新済みチェック
        if (sub.isRenewed()) {
            return "この契約は既に更新済みです（更新日: " + sub.getRenewedAt() + "）";
        }

        // 更新可能期間チェック
        if (!sub.isRenewable(today)) {
            if (sub.isExpired(today)) {
                return "この契約は期限切れのため更新できません";
            }
            return "この契約はまだ更新可能期間に入っていません"
                    + "（更新可能: "
                    + sub.getEndDate().minusMonths(DeadlineConstants.RENEWABLE_MONTHS)
                    + " 〜）";
        }

        // ⑤ 早期更新か通常更新かを判定
        LocalDate earlyDeadline = sub.getEndDate().minusDays(DeadlineConstants.EARLY_RENEWAL_DAYS);
        boolean isEarly = !today.isAfter(earlyDeadline); // today <= earlyDeadline なら早期

        // 更新実行（終了日を1年延長 + 更新日を記録）
        LocalDate oldEndDate = sub.getEndDate();
        sub.renew(today);

        String renewalType = isEarly ? "【早期更新】" : "【通常更新】";
        return renewalType + " 契約「" + sub.getName() + "」を更新しました"
                + "\n  更新日: " + today
                + "\n  旧満期日: " + oldEndDate
                + "\n  新満期日: " + sub.getEndDate()
                + "\n  早期更新基準日（満期" + DeadlineConstants.EARLY_RENEWAL_DAYS + "日前）: " + earlyDeadline;
    }

    // ⑥ 全契約一覧（期限状態付き）
    public List<String> listAll() {
        LocalDate today = LocalDate.now();
        return repository.findAll().stream()
                .map(sub -> sub.toString() + " → " + buildDeadlineStatus(sub, today))
                .collect(Collectors.toList());
    }

    // ⑦ 期限が近い契約を絞り込む（今日から指定日数以内に満期が来る契約）
    public List<Subscription> findApproaching(int withinDays) {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(withinDays);
        return repository.findAll().stream()
                // 未更新 かつ 満期日が today〜limit の範囲内
                .filter(sub -> !sub.isRenewed())
                .filter(sub -> !sub.getEndDate().isBefore(today))
                .filter(sub -> !sub.getEndDate().isAfter(limit))
                .collect(Collectors.toList());
    }

    // ⑧ IDで検索し、見つからなければ例外をスロー
    private Subscription findByIdOrThrow(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException(
                        "契約ID=" + id + " が見つかりません"));
    }

    // ⑨ 期限状態を判定して文字列に変換する（内部メソッド）
    private String buildDeadlineStatus(Subscription sub, LocalDate today) {
        // 更新済み
        if (sub.isRenewed()) {
            // 更新日が早期更新期限以前なら「早期更新済み」
            LocalDate earlyDeadline = sub.getEndDate().minusDays(DeadlineConstants.EARLY_RENEWAL_DAYS);
            // ※更新後はendDateが延長されているので、renewedAtベースで判定
            //  （実運用では renewalDueEndDate に旧満期日を保存して判定する）
            return "更新済み（更新日: " + sub.getRenewedAt() + "）";
        }

        // 期限切れ
        if (sub.isExpired(today)) {
            long overdueDays = ChronoUnit.DAYS.between(sub.getEndDate(), today);
            return "期限切れ（" + overdueDays + "日超過）";
        }

        // 更新可能期間内
        if (sub.isRenewable(today)) {
            LocalDate earlyDeadline = sub.getEndDate().minusDays(DeadlineConstants.EARLY_RENEWAL_DAYS);
            long daysUntilEnd = ChronoUnit.DAYS.between(today, sub.getEndDate());

            if (!today.isAfter(earlyDeadline)) {
                // 早期更新期間内
                long daysUntilEarlyDeadline = ChronoUnit.DAYS.between(today, earlyDeadline);
                return "更新可能【早期更新期間内】（早期更新期限まで残り" + daysUntilEarlyDeadline + "日 / 満期まで残り" + daysUntilEnd + "日）";
            } else {
                // 通常更新期間（早期更新期限を過ぎた）
                return "更新可能【通常期間】（満期まで残り" + daysUntilEnd + "日）";
            }
        }

        // まだ更新可能期間に入っていない
        long daysUntilEnd = ChronoUnit.DAYS.between(today, sub.getEndDate());
        LocalDate renewableStart = sub.getEndDate().minusMonths(DeadlineConstants.RENEWABLE_MONTHS);
        long daysUntilRenewable = ChronoUnit.DAYS.between(today, renewableStart);
        return "期限前（更新可能まで残り" + daysUntilRenewable + "日 / 満期まで残り" + daysUntilEnd + "日）";
    }
}
