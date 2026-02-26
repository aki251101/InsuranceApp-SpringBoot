package jp.yoshiaki.insuranceapp.training.day87.taskboard;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

/**
 * タスクボードの REST API コントローラ。
 * URL は /api/day87/tasks で、他の Day と衝突しない。
 */
@RestController("day87TaskController")
@RequestMapping("/api/day87/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * タスク作成（POST /api/day87/tasks）
     * リクエストボディ例: {"title": "買い物に行く"}
     */
    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        if (title == null || title.isBlank()) {
            // タイトルが空の場合は 400 Bad Request
            return ResponseEntity.badRequest().build();
        }
        Task created = taskService.create(title);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 全タスク取得（GET /api/day87/tasks）
     */
    @GetMapping
    public ResponseEntity<List<Task>> getAll() {
        List<Task> tasks = taskService.findAll();
        return ResponseEntity.ok(tasks);
    }

    /**
     * タスク完了（PUT /api/day87/tasks/{id}/complete）
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<Task> complete(@PathVariable Long id) {
        try {
            Task completed = taskService.complete(id);
            return ResponseEntity.ok(completed);
        } catch (RuntimeException e) {
            // ① 見つからない場合は 404 を返す
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * タスク削除（DELETE /api/day87/tasks/{id}）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            taskService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
