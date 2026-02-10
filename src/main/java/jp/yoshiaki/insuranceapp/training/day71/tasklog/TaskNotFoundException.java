package jp.yoshiaki.insuranceapp.training.day71.tasklog;

/**
 * タスクが見つからない場合の業務例外。
 * RuntimeExceptionを継承しているため、throws宣言は不要。
 */
public class TaskNotFoundException extends RuntimeException {

    private final int taskId;

    public TaskNotFoundException(int taskId) {
        super("タスクが見つかりません（ID: " + taskId + "）"); // ① メッセージに原因IDを含める
        this.taskId = taskId;
    }

    /** 見つからなかったタスクのIDを返す（ログ出力で使う） */
    public int getTaskId() {
        return taskId;
    }
}
