package jp.yoshiaki.insuranceapp.repository;

import jp.yoshiaki.insuranceapp.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 契約Repository
 * データベースの policies テーブルへのアクセスを担当
 */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    /**
     * 契約番号で検索
     *
     * @param policyNumber 契約番号
     * @return 契約データ
     */
    Optional<Policy> findByPolicyNumber(String policyNumber);

    /**
     * 契約番号または契約者名で部分一致検索
     *
     * @param policyNumber 契約番号（部分一致）
     * @param customerName 契約者名（部分一致）
     * @return 契約リスト
     */
    List<Policy> findByPolicyNumberContainingOrCustomerNameContaining(
            String policyNumber, String customerName);

    /**
     * ステータスで検索（満期日が近い順にソート）
     *
     * @param status ステータス
     * @return 契約リスト
     */
    List<Policy> findByStatusOrderByEndDateAscPolicyNumberAsc(String status);

    /**
     * 【修正1】契約中（ACTIVEかつ満期未到来）の契約を取得
     *
     *   旧: findByStatusOrderByEndDateAscPolicyNumberAsc("ACTIVE") を使用
     *        → status=ACTIVE だが endDate < today（失効）の契約も含まれていた
     *   新: status=ACTIVE かつ endDate >= today の契約のみ取得
     *        → 失効済みの契約は「契約中」タブに表示されない
     *
     * @param today 今日の日付
     * @return 契約リスト（失効を除いた契約中の契約のみ）
     */
    @Query("SELECT p FROM Policy p WHERE p.status = 'ACTIVE' " +
            "AND p.endDate >= :today " +
            "ORDER BY p.endDate ASC, p.policyNumber ASC")
    List<Policy> findActivePolicies(@Param("today") LocalDate today);

    /**
     * 満期日が指定期間内の契約を検索
     *
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 契約リスト
     */
    @Query("SELECT p FROM Policy p WHERE p.endDate BETWEEN :startDate AND :endDate " +
            "ORDER BY p.endDate ASC, p.policyNumber ASC")
    List<Policy> findByEndDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 更新可能期間内の契約を検索（ステータスがACTIVE）
     * 満期2ヶ月前〜満期日
     *
     * 【修正】クエリ条件を修正
     *   旧: endDate >= :twoMonthsBefore AND endDate >= :today（上限なし・重複条件）
     *   新: endDate BETWEEN :today AND :twoMonthsLater（正しい範囲指定）
     *
     * @param today 今日の日付
     * @param twoMonthsLater 2ヶ月後の日付
     * @return 契約リスト
     */
    @Query("SELECT p FROM Policy p WHERE p.status = 'ACTIVE' " +
            "AND p.endDate BETWEEN :today AND :twoMonthsLater " +
            "ORDER BY p.endDate ASC, p.policyNumber ASC")
    List<Policy> findRenewablePolicies(
            @Param("today") LocalDate today,
            @Param("twoMonthsLater") LocalDate twoMonthsLater);

    /**
     * 満期日が過ぎていて、かつ解約ではない契約を検索（失効）
     *
     * @param today 今日の日付
     * @return 契約リスト
     */
    @Query("SELECT p FROM Policy p WHERE p.endDate < :today " +
            "AND p.status != 'CANCELLED' " +
            "ORDER BY p.endDate ASC, p.policyNumber ASC")
    List<Policy> findLapsedPolicies(@Param("today") LocalDate today);

    /**
     * 更新済み契約を検索（更新前満期日が指定期間内）
     * 早期更改率の計算に使用
     *
     * 【修正】分母の絞り込み条件を修正
     *   旧: renewed_at の日付範囲で検索（年度ズレが発生するケースあり）
     *   新: renewal_due_end_date（nullならend_date）が指定期間内にある契約を検索
     *        かつ renewed_at が今日以前（更新済み確定分のみ）
     *
     * @param periodStart 期間開始日（年度開始日 or 月初日）
     * @param periodEnd 期間終了日（年度終了日 or 月末日）
     * @param today 今日の日付（renewed_at <= today の絞り込み用）
     * @return 契約リスト
     */
    @Query("SELECT p FROM Policy p WHERE p.renewedAt IS NOT NULL " +
            "AND DATE(p.renewedAt) <= :today " +
            "AND COALESCE(p.renewalDueEndDate, p.endDate) BETWEEN :periodStart AND :periodEnd")
    List<Policy> findRenewedPoliciesInPeriod(
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd,
            @Param("today") LocalDate today);

    /**
     * 【修正2】契約番号自動附番用 — 指定プレフィックスで始まる最大の契約番号を取得
     *
     * 契約番号の形式: P-{年度}-{連番4桁} (例: P-2026-0001)
     * MAX() で文字列の辞書順最大値を取得し、末尾の連番部分から次の番号を決定する
     *
     * @param prefix 契約番号プレフィックス（例: "P-2026-"）
     * @return 最大の契約番号（該当なしの場合はnull）
     */
    @Query("SELECT MAX(p.policyNumber) FROM Policy p WHERE p.policyNumber LIKE :prefix%")
    String findMaxPolicyNumberByPrefix(@Param("prefix") String prefix);
}
