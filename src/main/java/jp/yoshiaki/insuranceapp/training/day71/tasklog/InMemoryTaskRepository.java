package jp.yoshiaki.insuranceapp.training.day71.tasklog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * タスクをメモリ上（Map）に保存するRepository実装。
 * DEBUGレベルでデータ操作を記録する（本番ではDEBUGを切ることで非表示にできる）。
 */
@Repository("day71InMemoryTaskRepository")
public class InMemoryTaskRepository implements TaskRepository {

    // ① LoggerFactoryからこのクラス専用のLoggerを取得する
    //    引数にクラスを渡すと、ログに「InMemoryTaskRepository」と表示される
    private static final Logger log = LoggerFactory.getLogger(InMemoryTaskRepository.class);

    private final Map<Integer, Task> store = new LinkedHashMap<>(); // ② 挿入順を保持する保管場所
    private final AtomicInteger sequence = new AtomicInteger(0);    // ③ ID採番用（スレッドセーフ）

    @Override
    public Task save(Task task) {
        int id = sequence.incrementAndGet();
        Task newTask = new Task(id, task.getTitle());
        store.put(id, newTask);

        // ④ DEBUGレベル：開発中のデータ追跡用。本番ではログレベルをINFO以上にすれば非表示になる
        log.debug("リポジトリ保存完了: id={}, title={}", id, newTask.getTitle());

        return newTask;
    }

    @Override
    public Optional<Task> findById(int id) {
        Task found = store.get(id);
        log.debug("リポジトリ検索: id={}, 結果={}", id, (found != null ? "あり" : "なし"));
        return Optional.ofNullable(found);
    }

    @Override
    public List<Task> findAll() {
        log.debug("リポジトリ全件取得: 件数={}", store.size());
        return new ArrayList<>(store.values());
    }

    @Override
    public boolean delete(int id) {
        Task removed = store.remove(id);
        if (removed != null) {
            log.debug("リポジトリ削除完了: id={}", id);
            return true;
        } else {
            log.debug("リポジトリ削除対象なし: id={}", id);
            return false;
        }
    }
}
