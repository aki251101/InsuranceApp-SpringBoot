package jp.yoshiaki.insuranceapp.training.day87.taskboard;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * タスクの業務ロジックを担当するサービスクラス。
 * Controller から呼ばれ、Repository を通じて DB にアクセスする。
 */
@Service("day87TaskService")
public class TaskService {

    private final TaskRepository taskRepository;

    // ① コンストラクタインジェクション（推奨：テスト時に差し替えやすい）
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * 新規タスクを作成して保存する。
     *
     * @param title タスクのタイトル
     * @return 保存されたタスク（IDが採番済み）
     */
    @Transactional
    public Task create(String title) {
        Task task = new Task(title);
        return taskRepository.save(task);
    }

    /**
     * 全タスクを取得する。
     *
     * @return タスクのリスト
     */
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    /**
     * 指定IDのタスクを完了にする。
     *
     * @param id タスクID
     * @return 完了に更新されたタスク
     * @throws RuntimeException IDが見つからない場合
     */
    @Transactional
    public Task complete(Long id) {
        // ② orElseThrow で「見つからない」を明確にする
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "タスクが見つかりません（ID: " + id + "）"));
        task.complete();
        return taskRepository.save(task);
    }

    /**
     * 指定IDのタスクを削除する。
     *
     * @param id タスクID
     * @throws RuntimeException IDが見つからない場合
     */
    @Transactional
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException(
                    "タスクが見つかりません（ID: " + id + "）");
        }
        taskRepository.deleteById(id);
    }
}
