package jp.yoshiaki.insuranceapp.training.day73.deadline;

import java.util.List;
import java.util.Optional;

/**
 * 締め切りタスクの保存窓口（Repository interface）
 *
 * 「データをどこに保存するか」を隠す境界。
 * 今回はメモリ保存だが、将来DBに差し替えても
 * Service側のコードは変えなくてよい。
 */
public interface DeadlineRepository {

    /**
     * タスクを保存する（新規作成）
     * IDが未設定の場合、実装側で自動採番する
     *
     * @param deadline 保存するタスク
     * @return ID付きで保存されたタスク
     */
    Deadline save(Deadline deadline);

    /**
     * IDでタスクを検索する
     *
     * @param id タスクID
     * @return 見つかればOptionalに包んで返す、無ければ空
     */
    Optional<Deadline> findById(Long id);

    /**
     * 全タスクを取得する
     *
     * @return 全タスクのリスト
     */
    List<Deadline> findAll();
}
