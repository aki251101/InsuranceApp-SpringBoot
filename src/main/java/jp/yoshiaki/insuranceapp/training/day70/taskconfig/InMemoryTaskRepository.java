package jp.yoshiaki.insuranceapp.training.day70.taskconfig;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * メモリ上にタスクを保存するRepository実装。
 * LinkedHashMapで挿入順を保持し、AtomicLongでID自動採番する。
 */
@Repository("day70InMemoryTaskRepository")  // ① Bean名を明示（他Dayとの衝突防止）
public class InMemoryTaskRepository implements TaskRepository {

    // タスクの保管場所（メモリ上のMap）
    private final Map<Long, Task> store = new LinkedHashMap<>();

    // ID自動採番用（スレッドセーフなカウンター）
    private final AtomicLong nextId = new AtomicLong(1L);

    @Override
    public Task save(Task task) {
        // 新規タスクにIDを振って保存する
        Task newTask = new Task(nextId.getAndIncrement(), task.getTitle());
        store.put(newTask.getId(), newTask);
        return newTask;
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public int count() {
        return store.size();
    }
}
