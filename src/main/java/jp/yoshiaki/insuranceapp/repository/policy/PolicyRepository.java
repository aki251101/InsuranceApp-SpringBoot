package jp.yoshiaki.insuranceapp.repository.policy;

import jp.yoshiaki.insuranceapp.domain.policy.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    /**
     * prefix検索（証券番号の前方一致）
     * JPQLでは LIKE :prefix% は不可なので CONCAT を使う。
     */
    @Query("SELECT p FROM Policy p WHERE p.policyNumber LIKE CONCAT(:prefix, '%')")
    List<Policy> findByPolicyNumberPrefix(@Param("prefix") String prefix);

    /**
     * キーワード検索（本番想定：証券番号/氏名/車両情報/契約内容 を横断）
     * - lower + like で大小文字差を吸収
     * - DBによっては関数利用でインデックスが効かないため、将来は専用カラム/全文検索を検討。
     */
    @Query("""
        SELECT p FROM Policy p
         WHERE
           lower(p.policyNumber) LIKE lower(CONCAT('%', :keyword, '%'))
           OR lower(p.customerName) LIKE lower(CONCAT('%', :keyword, '%'))
           OR lower(coalesce(p.vehicleInfo, '')) LIKE lower(CONCAT('%', :keyword, '%'))
           OR lower(coalesce(p.contractContent, '')) LIKE lower(CONCAT('%', :keyword, '%'))
    """)
    List<Policy> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 当年度（期間）に「更新満了(renewalDueEndDate)」が入っている契約のうち、
     * 更新済み（renewedAt != null）のものを取得。
     *
     * RenewalStatsService（年次/期次の統計）で使用。
     */
    @Query("""
        SELECT p FROM Policy p
         WHERE p.renewalDueEndDate BETWEEN :start AND :end
           AND p.renewedAt IS NOT NULL
    """)
    List<Policy> findRenewedPoliciesInPeriod(@Param("start") LocalDate start,
                                             @Param("end") LocalDate end);
}
