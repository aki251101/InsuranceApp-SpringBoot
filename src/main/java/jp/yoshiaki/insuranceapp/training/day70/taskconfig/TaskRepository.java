package jp.yoshiaki.insuranceapp.training.day70.taskconfig;

import java.util.List;

/**
 * タスクの保存窓口（interface）。
 * 実装を差し替え可能にするために、interfaceとして定義する。
 */
public interface TaskRepository {

    /** タスクを保存して返す */
    Task save(Task task);

    /** 全タスクを取得する */
    List<Task> findAll();

    /** 現在の登録件数を返す */
    int count();
}
