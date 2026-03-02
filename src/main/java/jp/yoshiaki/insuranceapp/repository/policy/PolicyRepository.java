// 配置：src/main/java/jp/yoshiaki/insuranceapp/repository/policy/PolicyRepository.java
package jp.yoshiaki.insuranceapp.repository.policy;

import jp.yoshiaki.insuranceapp.domain.policy.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 契約Repository
 * policies テーブルへのデータアクセスを担当する。
 *
 * JpaRepository<Policy, Long> を extends するだけで、
 * save / findById / findAll / deleteById 等の基本CRUDが自動で使える。
 * 追加の検索条件が必要な場合は、命名規則クエリ or @Query で定義する。
 */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    // ── 命名規則クエリ（メソッド名からSQLを自動生成） ──────────

    /**
     * 契約番号（完全一致）で検索する。
     * 契約番号はユニークなので、結果は0件または1件。
     *
     * @param policyNumber 契約番号（例："P-2026-0001"）
     * @return 見つかれば Optional に包んで返す / なければ empty
     */
    Optional<Policy> findByPolicyNumber(String policyNumber);

    /**
     * 契約番号 OR 契約者名で部分一致検索する。
     * Spring Data JPA が「Containing」を LIKE '%...%' に変換する。
     *
     * @param policyNumber 契約番号の部分文字列
     * @param customerName 契約者名の部分文字列
     * @return 該当する契約リスト
     */
    List<Policy> findByPolicyNumberContainingOrCustomerNameContaining(
            String policyNumber, String customerName);

    /**
     * ステータス指定で検索する（満期日が近い順にソート）。
     *
     * @param status ステータス（"ACTIVE" / "CANCELLED"）
     * @return 契約リスト
     */
    List<Policy> findByStatusOrderByEndDateAscPolicyNumberAsc(String status);

    // ── @Query（JPQL）で書くカスタムクエリ ──────────

    /**
     * 契約中（ACTIVE かつ 満期日が今日以降）の契約を取得する。
     * status=ACTIVE でも endDate < today の場合は「失効」なので除外する。
     *
     * @param today 今日の日付
     * @return 有効な契約中のリスト
     */
    @Query("SELECT p FROM Policy p WHERE p.status = 'ACTIVE' " +
           "AND p.endDate >= :today " +
           "ORDER BY p.endDate ASC, p.policyNumber ASC")
    List<Policy> findActivePolicies(@Param("today") LocalDate today);

    /**
     * 満期日が指定期間内の契約を検索する。
     *
     * @param startDate 開始日
     * @param endDate   終了日
     * @return 契約リスト
     */
    @Query("SELECT p FROM Policy p WHERE p.endDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.endDate ASC, p.policyNumber ASC")
    List<Policy> findByEndDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 更新可能期間内の契約を検索する（ACTIVE かつ 満期日が today〜2ヶ月後）。
     *
     * @param today          今日の日付
     * @param twoMonthsLater 2ヶ月後の日付
     * @return 更新可能な契約リスト
     */
    @Query("SELECT p FROM Policy p WHERE p.status = 'ACTIVE' " +
           "AND p.endDate BETWEEN :today AND :twoMonthsLater " +
           "ORDER BY p.endDate ASC, p.policyNumber ASC")
    List<Policy> findRenewablePolicies(
            @Param("today") LocalDate today,
            @Param("twoMonthsLater") LocalDate twoMonthsLater);

    /**
     * 失効契約を検索する（満期日が過ぎていて、かつ解約ではない）。
     *
     * @param today 今日の日付
     * @return 失効した契約リスト
     */
    @Query("SELECT p FROM Policy p WHERE p.endDate < :today " +
           "AND p.status != 'CANCELLED' " +
           "ORDER BY p.endDate ASC, p.policyNumber ASC")
    List<Policy> findLapsedPolicies(@Param("today") LocalDate today);

    /**
     * 契約番号の自動附番用。
     * 指定プレフィックス（例："P-2026-"）で始まる契約番号のうち、
     * 辞書順で最大のものを返す。
     *
     * @param prefix プレフィックス（例："P-2026-"）
     * @return 最大の契約番号（該当なしなら null）
     */
    @Query("SELECT MAX(p.policyNumber) FROM Policy p WHERE p.policyNumber LIKE :prefix%")
    String findMaxPolicyNumberByPrefix(@Param("prefix") String prefix);
}
