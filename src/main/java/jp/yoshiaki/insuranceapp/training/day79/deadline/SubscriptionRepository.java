package jp.yoshiaki.insuranceapp.training.day79.deadline;

import java.util.List;
import java.util.Optional;

/**
 * サブスクリプションの保存窓口（interface）。
 * 「どこに保存するか」は実装クラス（InMemory / DB等）に任せる。
 *
 * 損保アプリでの対応：PolicyRepository（Spring Data JPA の interface）
 */
public interface SubscriptionRepository {

    // 契約を保存し、ID付きの契約を返す
    Subscription save(Subscription subscription);

    // IDで契約を検索（見つからなければ空のOptional）
    Optional<Subscription> findById(long id);

    // 全契約を返す
    List<Subscription> findAll();
}
