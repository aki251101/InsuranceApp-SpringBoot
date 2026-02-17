package jp.yoshiaki.insuranceapp.training.day70.taskconfig;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * タスクのCRUD操作を提供するRESTコントローラ。
 *
 * TaskServiceを経由して業務処理を行う。
 * 上限超過時の例外は @ExceptionHandler でキャッチして400応答に変換する。
 */
@Profile("training")
@RestController("day70TaskController")  // Bean名を明示（他Dayとの衝突防止）
@RequestMapping("/api/day70/tasks")
public class TaskController {

    private final TaskService taskService;

    // コンストラクタ注入：TaskServiceをSpringが自動で渡してくれる
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * POST /api/day70/tasks
     * リクエストボディから title を受け取り、タスクを作成する。
     * 成功時は 201 Created でタスクを返す。
     */
    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        Task created = taskService.create(title);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/day70/tasks
     * 全タスクを一覧で返す。
     */
    @GetMapping
    public List<Task> list() {
        return taskService.findAll();
    }

    /**
     * TaskLimitException をキャッチして 400 Bad Request を返す。
     * このコントローラ内で発生した TaskLimitException のみ対象。
     */
    @ExceptionHandler(TaskLimitException.class)
    public ResponseEntity<Map<String, String>> handleTaskLimit(TaskLimitException ex) {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("error", "TASK_LIMIT_EXCEEDED");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
