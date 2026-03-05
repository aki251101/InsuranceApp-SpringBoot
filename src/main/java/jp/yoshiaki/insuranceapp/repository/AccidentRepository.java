package jp.yoshiaki.insuranceapp.repository;

import jp.yoshiaki.insuranceapp.entity.Accident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 事故Repository
 * データベースの accidents テーブルへのアクセスを担当
 */
@Repository
public interface AccidentRepository extends JpaRepository<Accident, Long> {

    /**
     * 契約IDで事故を検索
     *
     * @param policyId 契約ID
     * @return 事故リスト
     */
    List<Accident> findByPolicyId(Long policyId);

    /**
     * ステータスで検索（事故受付日が新しい順）
     *
     * @param status ステータス
     * @return 事故リスト
     */
    List<Accident> findByStatusOrderByOccurredAtDesc(String status);

    /**
     * 複数ステータスで検索（事故受付日が新しい順）
     *
     * @param statuses ステータスリスト
     * @return 事故リスト
     */
    List<Accident> findByStatusInOrderByOccurredAtDesc(List<String> statuses);

    /**
     * すべての事故を取得（事故受付日が新しい順）
     *
     * @return 事故リスト
     */
    @Query("SELECT a FROM Accident a ORDER BY a.occurredAt DESC")
    List<Accident> findAllOrderByOccurredAtDesc();

    /**
     * 契約番号で事故を検索（Policyとjoin）
     *
     * @param policyNumber 契約番号
     * @return 事故リスト
     */
    @Query("SELECT a FROM Accident a JOIN a.policy p " +
            "WHERE p.policyNumber = :policyNumber " +
            "ORDER BY a.occurredAt DESC")
    List<Accident> findByPolicyNumber(@Param("policyNumber") String policyNumber);
}
