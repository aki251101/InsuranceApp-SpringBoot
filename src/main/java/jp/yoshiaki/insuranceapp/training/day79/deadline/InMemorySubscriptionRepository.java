package jp.yoshiaki.insuranceapp.training.day79.deadline;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * サブスクリプションをメモリ上に保管するRepository実装。
 * LinkedHashMapで登録順を保持する。
 *
 * 損保アプリでの対応：Spring Data JPA が自動生成するRepository実装に相当
 *（学習版ではDBを使わず、Mapで代替する）
 */
public class InMemorySubscriptionRepository implements SubscriptionRepository {

    // ① IDの自動採番（スレッドセーフなカウンター）
    private final AtomicLong idGenerator = new AtomicLong(1);

    // ② 保管場所（登録順を保持するLinkedHashMap）
    private final Map<Long, Subscription> store = new LinkedHashMap<>();

    @Override
    public Subscription save(Subscription subscription) {
        // ③ 新規登録：IDを自動採番して保存
        //    ※学習版のため、IDはコンストラクタで受け取る形にしている
        store.put(subscription.getId(), subscription);
        return subscription;
    }

    // ④ IDで検索：見つかればOptional.of、なければOptional.empty
    @Override
    public Optional<Subscription> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    // ⑤ 全件取得：Mapの値を新しいリストにコピーして返す
    @Override
    public List<Subscription> findAll() {
        return new ArrayList<>(store.values());
    }

    /**
     * 次のIDを発行する（save前にIDを決めるために使用）
     */
    public long nextId() {
        return idGenerator.getAndIncrement();
    }
}
