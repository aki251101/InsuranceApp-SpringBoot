package jp.yoshiaki.insuranceapp.training.day70.taskconfig;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * タスクの業務操作を担うServiceクラス。
 *
 * AppConfig から最大登録数を取得し、上限チェックを行う。
 * 設定値をコードに直書きせず、外部設定（yml）経由で受け取るのがポイント。
 */
@Profile("training")
@Service("day70TaskService")  // Bean名を明示（他Dayとの衝突防止）
public class TaskService {

    private final TaskRepository taskRepository;
    private final AppConfig appConfig;

    // コンストラクタ注入：RepositoryとAppConfigをSpringが自動で渡してくれる
    public TaskService(TaskRepository taskRepository, AppConfig appConfig) {
        this.taskRepository = taskRepository;
        this.appConfig = appConfig;
    }

    /**
     * タスクを新規作成する。
     * 最大登録数を超えている場合は TaskLimitException をスローする。
     */
    public Task create(String title) {
        // ① 現在の件数を取得
        int currentCount = taskRepository.count();
        // ② 設定ファイルの最大登録数と比較
        int maxTasks = appConfig.getMaxTasks();

        if (currentCount >= maxTasks) {
            // ③ 上限超過 → 業務例外をスロー（原因が追えるメッセージを付ける）
            throw new TaskLimitException(
                    "タスク登録数が上限に達しています（現在: " + currentCount
                            + "件, 上限: " + maxTasks + "件）"
            );
        }

        // ④ 上限内なので保存する（IDはRepository側で自動採番）
        Task task = new Task(null, title);
        return taskRepository.save(task);
    }

    /**
     * 全タスクを取得する。
     */
    public List<Task> findAll() {
        return taskRepository.findAll();
    }
}
