package jp.yoshiaki.insuranceapp.training.day70.taskconfig;

/**
 * タスクの最大登録数を超えた場合にスローする業務例外。
 *
 * RuntimeException を継承しているため、メソッドの throws 宣言が不要。
 * （Spring の @ExceptionHandler で一括キャッチする想定）
 */
public class TaskLimitException extends RuntimeException {

    public TaskLimitException(String message) {
        super(message);
    }
}
