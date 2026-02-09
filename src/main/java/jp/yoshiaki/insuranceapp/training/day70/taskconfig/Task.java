package jp.yoshiaki.insuranceapp.training.day70.taskconfig;

/**
 * タスクを表すドメインクラス。
 * IDとタイトルを保持するシンプルな構造。
 */
public class Task {

    private final Long id;
    private final String title;

    // コンストラクタ：IDとタイトルを受け取って不変オブジェクトを作る
    public Task(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", title='" + title + "'}";
    }
}
