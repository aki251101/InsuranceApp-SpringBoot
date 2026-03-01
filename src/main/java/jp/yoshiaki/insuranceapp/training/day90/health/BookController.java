package jp.yoshiaki.insuranceapp.training.day90.health;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 書籍の REST API コントローラー。
 * Actuator のエンドポイント（/actuator/health）とは別の
 * 「業務用API」であることを体感するための実装。
 */
@RestController("day90BookController")  // Bean名を明示（他Dayとの衝突防止）
@RequestMapping("/api/day90/books")     // Day90 専用のURLパス
public class BookController {

    private final BookService service;

    // ── コンストラクタインジェクション ──
    public BookController(BookService service) {
        this.service = service;
    }

    /**
     * 書籍を登録する。
     *
     * リクエストボディ例:
     * {
     *   "title": "Spring Boot入門",
     *   "author": "山田太郎",
     *   "stock": 10
     * }
     */
    @PostMapping
    public ResponseEntity<Book> create(@RequestBody Map<String, Object> request) {
        // ① リクエストから値を取り出す
        String title = (String) request.get("title");
        String author = (String) request.get("author");
        int stock = (int) request.get("stock");

        // ② Service に委譲して作成
        Book created = service.create(title, author, stock);

        // ③ 201 Created で返す
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 全書籍を取得する。
     */
    @GetMapping
    public ResponseEntity<List<Book>> findAll() {
        List<Book> books = service.findAll();
        return ResponseEntity.ok(books);
    }
}
