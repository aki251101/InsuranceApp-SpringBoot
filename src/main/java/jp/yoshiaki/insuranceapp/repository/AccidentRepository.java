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
 *
 * Spring Data JPA が interface の定義だけで実装クラスを自動生成する。
 * メソッド名の命名規則に従えば、SQLを書かずに検索条件を定義できる。
 */
@Repository
public interface AccidentRepository extends JpaRepository<Accident, Long> {

    /**
     * 契約IDで事故を検索する
     *
     * メソッド名 findByPolicyId → WHERE policy_id = ? に自動変換される
     *
     * @param policyId 契約ID
     * @return その契約に紐づく事故リスト
     */
    List<Accident> findByPolicyId(Long policyId);

    /**
     * ステータスで検索し、事故受付日の新しい順に並べる
     *
     * メソッド名の分解:
     *   findBy         → WHERE
     *   Status         → status = ?
     *   OrderBy        → ORDER BY
     *   OccurredAtDesc → occurred_at DESC（新しい順）
     *
     * @param status ステータス（例: "RESOLVED"）
     * @return 事故リスト（受付日が新しい順）
     */
    List<Accident> findByStatusOrderByOccurredAtDesc(String status);

    /**
     * 複数ステータスで検索し、事故受付日の新しい順に並べる
     *
     * メソッド名の分解:
     *   findBy            → WHERE
     *   StatusIn          → status IN (?, ?, ...)
     *   OrderBy           → ORDER BY
     *   OccurredAtDesc    → occurred_at DESC
     *
     * 用途: 「対応中」タブで OPEN + IN_PROGRESS をまとめて取得する
     *
     * @param statuses ステータスのリスト（例: ["OPEN", "IN_PROGRESS"]）
     * @return 事故リスト（受付日が新しい順）
     */
    List<Accident> findByStatusInOrderByOccurredAtDesc(List<String> statuses);

    /**
     * すべての事故を事故受付日の新しい順で取得する
     *
     * JpaRepository の findAll() には並び順の指定がないため、
     * @Query で JPQL を直接書いて ORDER BY を指定する
     *
     * ※ JPQL はテーブル名ではなくエンティティ名（Accident）を使う
     *
     * @return 全事故リスト（受付日が新しい順）
     */
    @Query("SELECT a FROM Accident a ORDER BY a.occurredAt DESC")
    List<Accident> findAllOrderByOccurredAtDesc();

    /**
     * 契約番号で事故を検索する（PolicyテーブルとJOIN）
     *
     * Accident → Policy のリレーション（a.policy）を使って
     * JOIN し、Policy の policyNumber で絞り込む
     *
     * @param policyNumber 契約番号（例: "P-2026-0001"）
     * @return 事故リスト（受付日が新しい順）
     */
    @Query("SELECT a FROM Accident a JOIN a.policy p " +
            "WHERE p.policyNumber = :policyNumber " +
            "ORDER BY a.occurredAt DESC")
    List<Accident> findByPolicyNumber(@Param("policyNumber") String policyNumber);
}
