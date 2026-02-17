package jp.yoshiaki.insuranceapp.training.day72.book;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Day72 動作確認用エントリポイント。
 * Spring Boot 起動時にサンプルデータを自動登録し、動作ログを出力する。
 * メインの動作確認は Postman で行う。
 */
@Profile("training")
@Component("day72Main")
public class Day72Main implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Day72Main.class);

    private final BookService bookService;

    public Day72Main(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void run(String... args) {
        log.info("=== Day72 REST設計ミニアプリ 起動 ===");

        // ① サンプルデータを登録
        Book book1 = bookService.create("Javaの基礎", "978-4-123-45678-0");
        Book book2 = bookService.create("Spring Boot入門", "978-4-234-56789-1");
        Book book3 = bookService.create("REST API設計ガイド", "978-4-345-67890-2");

        log.info("サンプルデータ登録完了: {}件", bookService.findAll().size());
        log.info("  - ID={}: {} (ISBN: {})", book1.getId(), book1.getTitle(), book1.getIsbn());
        log.info("  - ID={}: {} (ISBN: {})", book2.getId(), book2.getTitle(), book2.getIsbn());
        log.info("  - ID={}: {} (ISBN: {})", book3.getId(), book3.getTitle(), book3.getIsbn());

        log.info("");
        log.info("Postman で以下のエンドポイントを試してください：");
        log.info("  POST   http://localhost:8080/api/day72/books       → 201 Created");
        log.info("  GET    http://localhost:8080/api/day72/books       → 200 OK（一覧）");
        log.info("  GET    http://localhost:8080/api/day72/books/1     → 200 OK（1件）");
        log.info("  PUT    http://localhost:8080/api/day72/books/1     → 200 OK（更新）");
        log.info("  DELETE http://localhost:8080/api/day72/books/1     → 204 No Content");
        log.info("  GET    http://localhost:8080/api/day72/books/999   → 404 Not Found");
        log.info("=== Postman での動作確認をお願いします ===");
    }
}
