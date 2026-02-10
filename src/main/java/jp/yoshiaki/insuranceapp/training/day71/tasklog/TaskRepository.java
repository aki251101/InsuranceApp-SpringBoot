package jp.yoshiaki.insuranceapp.training.day71.tasklog;

import java.util.List;
import java.util.Optional;

/**
 * タスクの保存・取得・削除を行う窓口（interface）。
 * 実装を差し替え可能にするために、interfaceとして定義する。
 */
public interface TaskRepository {

    /** タスクを保存し、保存されたTaskを返す */
    Task save(Task task);

    /** IDでタスクを検索する。見つからない場合はOptional.empty() */
    Optional<Task> findById(int id);

    /** 全タスクをリストで返す */
    List<Task> findAll();

    /** IDを指定してタスクを削除する。削除できたらtrue */
    boolean delete(int id);
}
