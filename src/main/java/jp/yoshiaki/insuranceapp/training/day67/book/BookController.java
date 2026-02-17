package jp.yoshiaki.insuranceapp.training.day67.book;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 書籍Controller（REST API）
 * HTTPリクエストを受け取り、Serviceを呼び出す
 * 
 * 【エンドポイント一覧】
 * - GET  /api/day67/books          → 全件取得
 * - GET  /api/day67/books/{id}     → 1件取得
 * - GET  /api/day67/books/search   → タイトル検索
 * - POST /api/day67/books          → 新規登録
 */
@Profile("training")
@RestController("day67BookController")
@RequestMapping("/api/day67/books")
public class BookController {

    private final BookService bookService;

    /**
     * コンストラクタインジェクション
     */
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 全書籍を取得
     * 
     * @return 全書籍リスト
     */
    @GetMapping
    public ResponseEntity<List<Book>> findAll() {
        List<Book> books = bookService.findAll();
        return ResponseEntity.ok(books);
    }

    /**
     * IDで書籍を取得
     * 
     * 【ポイント】
     * - @PathVariable：URLの{id}部分をLong型で受け取る
     * - 見つからない場合はServiceがNotFoundExceptionを投げる
     * - GlobalExceptionHandlerが404に変換してくれる
     * 
     * @param id 書籍ID
     * @return 書籍
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> findById(@PathVariable Long id) {
        Book book = bookService.findById(id);
        return ResponseEntity.ok(book);
    }

    /**
     * タイトルで検索
     * 
     * @param keyword 検索キーワード（省略可能）
     * @return 該当書籍リスト
     */
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchByTitle(
            @RequestParam(required = false) String keyword) {
        List<Book> books = bookService.searchByTitle(keyword);
        return ResponseEntity.ok(books);
    }

    /**
     * 書籍を新規登録
     * 
     * 【ポイント】
     * - @RequestBody：リクエストボディのJSONをBook型に変換
     * - HttpStatus.CREATED：登録成功時は201を返す（200ではない）
     * 
     * @param book 登録する書籍
     * @return 登録された書籍
     */
    @PostMapping
    public ResponseEntity<Book> create(@RequestBody Book book) {
        Book created = bookService.create(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
