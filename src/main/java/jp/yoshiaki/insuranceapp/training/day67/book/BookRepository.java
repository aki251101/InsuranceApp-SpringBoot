package jp.yoshiaki.insuranceapp.training.day67.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

 /**
 * 書籍Repository
 * JpaRepositoryを継承するだけで、基本CRUDメソッドが自動生成される
 *
 * 【ポイント】
 * - JpaRepository<Book, Long> の意味：
 *   - Book：操作対象のEntity
 *   - Long：主キー（ID）の型
 * - 自動で使えるメソッド：
 *   - findById(Long id) → Optional<Book>
 *   - findAll() → List<Book>
 *   - save(Book book) → Book
 *   - deleteById(Long id) → void
 *   など多数
 */
@Repository("day67BookRepository")
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * タイトル部分一致検索
     * 
     * 【メソッド名クエリ】
     * メソッド名「findByTitleContaining」をSpringが解析し、
     * 「SELECT * FROM day67_books WHERE title LIKE '%keyword%'」相当のSQLを自動生成する
     * 
     * @param keyword 検索キーワード
     * @return 該当する書籍リスト
     */
    List<Book> findByTitleContaining(String keyword);

    /**
     * 著者名で検索
     * 
     * @param author 著者名（完全一致）
     * @return 該当する書籍リスト
     */
    List<Book> findByAuthor(String author);
}
