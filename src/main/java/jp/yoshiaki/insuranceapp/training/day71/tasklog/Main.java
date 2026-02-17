package jp.yoshiaki.insuranceapp.training.day71.tasklog;

import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * コンソールからタスク操作を行うCLIアプリ。
 * CommandLineRunnerを実装し、Spring Boot起動後に自動でrun()が呼ばれる。
 *
 * ログの使い方：
 *   - コマンド受付 → DEBUG（開発時の追跡用）
 *   - 操作失敗の表示 → WARN（ユーザーの入力ミスで起こり得る）
 *   - 予期しないエラー → ERROR（本来起きないはずの異常）
 */
@Profile("training")
@Component("day71Main")
public class Main implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private final TaskService taskService;

    public Main(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== タスク管理アプリ（ログ実践版）===");
        System.out.println("操作を入力してください（helpで一覧、exitで終了）");
        System.out.println();

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            // ① コマンド受付をDEBUGログで記録（開発時の追跡用）
            log.debug("コマンド受付: {}", line);

            String[] parts = line.split("\\s+", 2);
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "add":
                        handleAdd(parts);
                        break;
                    case "list":
                        handleList(parts);
                        break;
                    case "done":
                        handleDone(parts);
                        break;
                    case "delete":
                        handleDelete(parts);
                        break;
                    case "help":
                        handleHelp();
                        break;
                    case "exit":
                        System.out.println("アプリを終了します。");
                        log.info("アプリ終了（ユーザー操作）");
                        return;
                    default:
                        System.out.println("不明なコマンドです: " + command);
                        System.out.println("helpで使い方を確認できます。");
                        log.warn("不明なコマンド入力: {}", command);
                        break;
                }
            } catch (TaskNotFoundException e) {
                // ② 想定内の業務エラー：ユーザーにメッセージを表示
                System.out.println(e.getMessage());
            } catch (IllegalArgumentException e) {
                // ③ 入力値の不正：ユーザーにメッセージを表示
                System.out.println("入力エラー: " + e.getMessage());
                log.warn("入力エラー: {}", e.getMessage());
            } catch (Exception e) {
                // ④ 想定外のエラー：ERRORログに記録（スタックトレース付き）
                System.out.println("予期しないエラーが発生しました。");
                log.error("予期しないエラー: {}", e.getMessage(), e);
            }

            System.out.println();
        }
    }

    /** タスク作成：add <タイトル> */
    private void handleAdd(String[] parts) {
        if (parts.length < 2 || parts[1].isBlank()) {
            throw new IllegalArgumentException("タイトルを指定してください（例：add 報告書作成）");
        }

        String title = parts[1];
        Task created = taskService.create(title);
        System.out.println("タスクを作成しました: " + created);
    }

    /** タスク一覧：list / list <状態> */
    private void handleList(String[] parts) {
        List<Task> tasks;

        if (parts.length >= 2 && !parts[1].isBlank()) {
            // 状態で絞り込み
            TaskStatus status = TaskStatus.parse(parts[1]);
            tasks = taskService.listByStatus(status);
            System.out.println("--- タスク一覧（" + status.getDisplayName() + "）---");
        } else {
            // 全件表示
            tasks = taskService.listAll();
            System.out.println("--- タスク一覧（全件）---");
        }

        if (tasks.isEmpty()) {
            System.out.println("  （タスクはありません）");
        } else {
            for (Task task : tasks) {
                System.out.println("  " + task);
            }
        }
    }

    /** タスク完了：done <ID> */
    private void handleDone(String[] parts) {
        int id = parseId(parts, "done");
        Task completed = taskService.complete(id);
        System.out.println("タスクを完了にしました: " + completed);
    }

    /** タスク削除：delete <ID> */
    private void handleDelete(String[] parts) {
        int id = parseId(parts, "delete");
        taskService.delete(id);
        System.out.println("タスクを削除しました（ID: " + id + "）");
    }

    /** ヘルプ表示 */
    private void handleHelp() {
        System.out.println("使い方:");
        System.out.println("  add <タイトル>    タスクを作成する");
        System.out.println("  list             全タスクを一覧表示する");
        System.out.println("  list <状態>      状態で絞り込む（done/完了/todo/未着手/doing/進行中）");
        System.out.println("  done <ID>        タスクを完了にする");
        System.out.println("  delete <ID>      タスクを削除する");
        System.out.println("  help             この使い方を表示する");
        System.out.println("  exit             アプリを終了する");
    }

    /**
     * コマンド引数からIDを取得する。
     * 数値でない場合はIllegalArgumentExceptionを投げる。
     */
    private int parseId(String[] parts, String commandName) {
        if (parts.length < 2 || parts[1].isBlank()) {
            throw new IllegalArgumentException("IDを指定してください（例：" + commandName + " 1）");
        }
        try {
            return Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("IDは数値で指定してください: " + parts[1]);
        }
    }
}
