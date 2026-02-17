package jp.yoshiaki.insuranceapp.training.day67.book;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 書籍Service
 * 書籍に関するビジネスロジックを実装
 * 
 * 【役割】
 * - Repositoryを呼び出し、Optionalの処理を行う
 * - 「見つからない」場合はNotFoundExceptionを投げる
 * - Controllerに渡す前にデータを加工する（今回は素通しだが）
 */
@Profile("training")
@Service("day67BookService")
@Transactional(readOnly = true)  // ① デフォルトは読み取り専用
public class BookService {

    private final BookRepository bookRepository;

    /**
     * コンストラクタインジェクション
     * 
     * 【なぜコンストラクタ引数で受け取るのか】
     * - Springが自動で BookRepository の実装を注入してくれる
     * - finalにすることで、注入後に差し替えられない（安全）
     * - テスト時にモックを注入しやすい
     */
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * 全書籍を取得
     * 
     * @return 全書籍リスト
     */
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    /**
     * IDで書籍を取得
     * 
     * 【ポイント：Optionalの処理】
     * - findById() は Optional<Book> を返す（nullではない）
     * - orElseThrow() で「中身がなければ例外を投げる」
     * - これにより「見つからない」ケースを明示的に処理できる
     * 
     * @param id 書籍ID
     * @return 書籍
     * @throws NotFoundException 見つからない場合
     */

    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("書籍が見つかりません: id=" + id));
    }

    /**
     * タイトルで検索（部分一致）
     * 
     * @param keyword 検索キーワード
     * @return 該当書籍リスト
     */
    public List<Book> searchByTitle(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();  // キーワードがなければ全件
        }
        return bookRepository.findByTitleContaining(keyword);
    }

    /**
     * 書籍を新規登録
     * 
     * @param book 登録する書籍
     * @return 登録された書籍（IDが採番される）
     */
    @Transactional  // ② 書き込みなのでreadOnly=falseに上書き
    public Book create(Book book) {
        return bookRepository.save(book);
    }
}
