package jp.yoshiaki.insuranceapp.training.day72.book;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
 * 書籍管理 REST API コントローラ。
 *
 * 【今日の核心】RESTful 設計の 3点セット：
 *   URL    → リソース名（名詞・複数形）: /api/day72/books
 *   メソッド → 操作を表す: GET=取得, POST=作成, PUT=更新, DELETE=削除
 *   ステータス → 結果を伝える: 200=OK, 201=Created, 204=NoContent, 404=NotFound, 409=Conflict
 */
@RestController("day72BookController")
@RequestMapping("/api/day72/books")  // ① ベースURL：リソース名は複数形名詞
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // ========================
    // CRUD エンドポイント
    // ========================

    /**
     * 【POST /api/day72/books】書籍を新規登録する。
     * 成功時：201 Created（「新しいリソースを作った」を明示）
     */
    @PostMapping
    public ResponseEntity<Book> create(@RequestBody BookRequest request) {
        // ② POST 成功は 201 Created が RESTful のルール
        Book created = bookService.create(request.getTitle(), request.getIsbn());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 【GET /api/day72/books】全件一覧を取得する。
     * 成功時：200 OK
     */
    @GetMapping
    public ResponseEntity<List<Book>> findAll() {
        // ③ 一覧取得は 200 OK（データが0件でも200。空リストを返す）
        return ResponseEntity.ok(bookService.findAll());
    }

    /**
     * 【GET /api/day72/books/{id}】ID指定で1件取得する。
     * 成功時：200 OK ／ 存在しない：404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> findById(@PathVariable Long id) {
        // ④ @PathVariable で URL の {id} 部分を受け取る
        return ResponseEntity.ok(bookService.findById(id));
    }

    /**
     * 【PUT /api/day72/books/{id}】書籍情報を更新する。
     * 成功時：200 OK ／ 存在しない：404 ／ ISBN重複：409
     */
    @PutMapping("/{id}")
    public ResponseEntity<Book> update(@PathVariable Long id, @RequestBody BookRequest request) {
        // ⑤ PUT は「リソース全体を置き換える」意味。部分更新は PATCH
        Book updated = bookService.update(id, request.getTitle(), request.getIsbn());
        return ResponseEntity.ok(updated);
    }

    /**
     * 【DELETE /api/day72/books/{id}】書籍を削除する。
     * 成功時：204 No Content（「消したけど返す中身はないよ」）
     * 存在しない：404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // ⑥ DELETE 成功は 204 No Content が RESTful のルール
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ========================
    // 例外ハンドラ（例外 → HTTPステータス変換）
    // ========================

    /**
     * BookNotFoundException → 404 Not Found
     */
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(BookNotFoundException e) {
        // ⑦ エラーレスポンスも JSON で返す（クライアントが解析しやすい）
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * DuplicateIsbnException → 409 Conflict
     */
    @ExceptionHandler(DuplicateIsbnException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(DuplicateIsbnException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }

    // ========================
    // リクエスト DTO（内部クラス）
    // ========================

    /**
     * POST/PUT のリクエストボディを受け取るクラス。
     * クライアントから { "title": "...", "isbn": "..." } の形で送られてくる。
     */
    public static class BookRequest {

        private String title;
        private String isbn;

        // Jackson（JSON変換ライブラリ）がデフォルトコンストラクタを使う
        public BookRequest() {
        }

        public BookRequest(String title, String isbn) {
            this.title = title;
            this.isbn = isbn;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }
    }
}
