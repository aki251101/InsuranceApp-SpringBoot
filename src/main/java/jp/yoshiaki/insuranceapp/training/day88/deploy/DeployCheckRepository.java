package jp.yoshiaki.insuranceapp.training.day88.deploy;

import java.util.List;
import java.util.Optional;

/**
 * デプロイチェック項目の保存窓口（interface）。
 * 実装を差し替えることで、メモリ保存→DB保存への切り替えが可能になる。
 */
public interface DeployCheckRepository {

    /** 全項目を返す */
    List<DeployCheckItem> findAll();

    /** IDで1件検索（見つからない場合はOptional.empty()） */
    Optional<DeployCheckItem> findById(int id);

    /** カテゴリで絞り込み */
    List<DeployCheckItem> findByCategory(CheckCategory category);
}
