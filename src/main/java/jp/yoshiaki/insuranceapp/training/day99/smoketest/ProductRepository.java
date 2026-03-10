package jp.yoshiaki.insuranceapp.training.day99.smoketest;

import java.util.List;
import java.util.Optional;

/**
 * 商品Repository（interface：保存窓口）
 * 保存先（InMemory/DB等）を差し替え可能にするための抽象層。
 * ヘルスチェックでは count() でDB疎通を確認する。
 */
public interface ProductRepository {

    /**
     * 全商品を取得する
     * @return 商品リスト
     */
    List<Product> findAll();

    /**
     * IDで商品を取得する
     * @param id 商品ID
     * @return 商品（見つからない場合はempty）
     */
    Optional<Product> findById(Long id);

    /**
     * 商品を保存する（新規登録）
     * @param product 商品データ
     * @return 保存された商品（IDが付与済み）
     */
    Product save(Product product);

    /**
     * 商品件数を返す（ヘルスチェック用：DB疎通確認）
     * @return 件数
     */
    long count();
}
