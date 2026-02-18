package jp.yoshiaki.insuranceapp.training.day79.deadline;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * サブスクリプション（定期契約）を表すドメインクラス。
 * 1件の契約データと、更新操作のメソッドを持つ。
 *
 * 損保アプリでの対応：Policy エンティティの
 *   startDate / endDate / renewedAt / isRenewable() に相当
 */
public class Subscription {

    // 表示用の日付フォーマット
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final long id;           // 契約ID（変更不可）
    private final String name;       // 契約名
    private final LocalDate startDate; // 契約開始日
    private LocalDate endDate;       // 契約終了日（満期日）：更新時に延長される
    private LocalDate renewedAt;     // 更新日（未更新ならnull）

    // ① コンストラクタ：必須項目を受け取る
    public Subscription(long id, String name, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.renewedAt = null; // 新規登録時は未更新
    }

    // ② 更新操作：終了日を1年延長し、更新日を記録する
    public void renew(LocalDate today) {
        this.renewedAt = today;
        this.endDate = this.endDate.plusYears(1); // 満期日を1年延長
    }

    // ③ 更新可能期間内か判定（満期N月前〜満期日）
    public boolean isRenewable(LocalDate today) {
        LocalDate renewableStart = endDate.minusMonths(DeadlineConstants.RENEWABLE_MONTHS);
        // todayが renewableStart以降 かつ endDate以前 なら更新可能
        return !today.isBefore(renewableStart) && !today.isAfter(endDate);
    }

    // ④ 満期日を過ぎているか判定
    public boolean isExpired(LocalDate today) {
        return today.isAfter(endDate);
    }

    // ⑤ 更新済みか判定
    public boolean isRenewed() {
        return renewedAt != null;
    }

    // --- getter ---
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getRenewedAt() {
        return renewedAt;
    }

    // ⑥ 表示用：日本語で契約情報をまとめる
    @Override
    public String toString() {
        String renewedInfo = (renewedAt != null)
                ? "（更新日: " + renewedAt.format(FORMATTER) + "）"
                : "（未更新）";
        return String.format("[ID:%d] %s | 期間: %s 〜 %s %s",
                id,
                name,
                startDate.format(FORMATTER),
                endDate.format(FORMATTER),
                renewedInfo);
    }
}
