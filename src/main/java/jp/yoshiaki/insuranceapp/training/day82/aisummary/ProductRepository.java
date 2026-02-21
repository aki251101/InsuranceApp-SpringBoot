package jp.yoshiaki.insuranceapp.training.day82.aisummary;

import java.util.List;
import java.util.Optional;

/**
 * 商品の保存・検索の窓口を定義するinterface。
 *
 * 実装を差し替え可能にする（InMemory → DB等）。
 */
public interface ProductRepository {

    /** 商品を保存し、ID採番済みの商品を返す */
    Product save(Product product);

    /** IDで商品を検索する（見つからない場合はOptional.empty） */
    Optional<Product> findById(long id);

    /** 全商品を一覧で返す */
    List<Product> findAll();
}
