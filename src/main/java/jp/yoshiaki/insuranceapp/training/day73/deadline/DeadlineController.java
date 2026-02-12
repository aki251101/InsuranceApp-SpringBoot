package jp.yoshiaki.insuranceapp.training.day73.deadline;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 締め切りタスクのREST APIコントローラー
 *
 * エンドポイント一覧：
 * - POST   /api/day73/deadlines           → タスク登録
 * - GET    /api/day73/deadlines           → タスク一覧
 * - PATCH  /api/day73/deadlines/{id}/complete → タスク完了
 */
@RestController("day73DeadlineController")
@RequestMapping("/api/day73/deadlines")
public class DeadlineController {

    private final DeadlineService service;

    // コンストラクタインジェクション
    public DeadlineController(DeadlineService service) {
        this.service = service;
    }

    /**
     * タスクを登録する
     *
     * リクエストボディ例：
     * {
     *   "title": "報告書提出",
     *   "dueDate": "2026-02-14"
     * }
     *
     * @param body リクエストボディ（JSONをMapで受け取る）
     * @return 登録されたタスク情報
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // ① 201 Created を返す
    public Map<String, Object> create(@RequestBody Map<String, String> body) {
        // ② リクエストからタイトルと締め切り日を取得
        String title = body.get("title");
        LocalDate dueDate = LocalDate.parse(body.get("dueDate"));

        // ③ Serviceに委譲して登録
        Deadline saved = service.create(title, dueDate);

        // ④ レスポンス用のMapを組み立てて返す
        return toResponse(saved);
    }

    /**
     * タスク一覧を取得する
     *
     * @return タスクの全件リスト
     */
    @GetMapping
    public List<Map<String, Object>> list() {
        return service.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * タスクを完了にする
     *
     * @param id タスクID（URLパスから取得）
     * @return 完了にしたタスク情報
     */
    @PatchMapping("/{id}/complete")
    public Map<String, Object> complete(@PathVariable Long id) {
        Deadline completed = service.complete(id);
        return toResponse(completed);
    }

    /**
     * Deadlineオブジェクトをレスポンス用Mapに変換する（private）
     *
     * DTOクラスを作らず、Mapで簡易的にJSON構造を作る方法。
     * 学習版ではシンプルさを優先してMapを使う。
     */
    private Map<String, Object> toResponse(Deadline d) {
        return Map.of(
                "id", d.getId(),
                "title", d.getTitle(),
                "dueDate", d.getDueDate().toString(),
                "status", d.getStatus().name(),
                "statusLabel", d.getStatus().getLabel(),
                "overdue", d.isOverdue(),
                "dueSoon", d.isDueSoon(),
                "daysUntilDue", d.daysUntilDue()
        );
    }
}
