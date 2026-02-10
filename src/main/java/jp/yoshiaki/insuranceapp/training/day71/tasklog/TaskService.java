package jp.yoshiaki.insuranceapp.training.day71.tasklog;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * タスクの業務操作を担うServiceクラス。
 * ログレベルの使い分けが今日の学習の核。
 *
 * ログレベルの方針：
 *   INFO  = 正常な業務操作の記録（作成・完了・削除の成功）
 *   WARN  = 想定内の失敗（存在しないIDへの操作、不正な状態遷移）
 *   ERROR = 想定外のシステムエラー（今回は使わないが、DB接続失敗等で使う）
 *   DEBUG = 開発中のデータ追跡（リスト件数、検索結果など）
 */
@Service("day71TaskService")
public class TaskService {

    // ① このクラス専用のLoggerを取得。ログには「TaskService」と表示される
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository repository;

    // ② コンストラクタインジェクション（DIの推奨パターン）
    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    /**
     * タスクを作成する。
     * 成功時にINFOログを記録する。
     */
    public Task create(String title) {
        // ③ プレースホルダ {} を使うことで、ログレベルが無効な場合に文字列結合が発生しない
        log.info("タスク作成開始: title={}", title);

        Task task = new Task(0, title); // IDは仮。Repository側で採番する
        Task saved = repository.save(task);

        log.info("タスク作成完了: id={}, title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    /**
     * 全タスクを取得する。
     * DEBUGログで件数を記録（本番では通常非表示）。
     */
    public List<Task> listAll() {
        List<Task> tasks = repository.findAll();
        log.debug("タスク一覧取得: 件数={}", tasks.size());
        return tasks;
    }

    /**
     * 指定された状態のタスクのみを取得する。
     */
    public List<Task> listByStatus(TaskStatus status) {
        List<Task> filtered = repository.findAll().stream()
                .filter(task -> task.getStatus() == status)
                .collect(Collectors.toList());

        log.debug("タスク状態別一覧: status={}, 件数={}", status.name(), filtered.size());
        return filtered;
    }

    /**
     * タスクを完了にする。
     * 存在しないIDの場合はWARNログを記録してから例外を投げる。
     */
    public Task complete(int id) {
        log.info("タスク完了処理開始: id={}", id);

        Task task = repository.findById(id)
                .orElseThrow(() -> {
                    // ④ WARNレベル：想定内の失敗。ユーザーの入力ミス等で起こり得る
                    log.warn("タスク完了失敗: id={} が存在しない", id);
                    return new TaskNotFoundException(id);
                });

        task.done();
        log.info("タスク完了成功: id={}, title={}", task.getId(), task.getTitle());
        return task;
    }

    /**
     * タスクを削除する。
     * 存在しないIDの場合はWARNログを記録してから例外を投げる。
     */
    public void delete(int id) {
        log.info("タスク削除処理開始: id={}", id);

        boolean deleted = repository.delete(id);
        if (!deleted) {
            // ⑤ WARNレベル：想定内の失敗
            log.warn("タスク削除失敗: id={} が存在しない", id);
            throw new TaskNotFoundException(id);
        }

        log.info("タスク削除成功: id={}", id);
    }
}
