package jp.yoshiaki.insuranceapp.training.day66.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jp.yoshiaki.insuranceapp.training.day66.jpa.domain.Day66Note;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("day66NoteRepository")  // Bean名を明示（衝突防止）
public class Day66NoteRepository {

    @PersistenceContext       // ① EntityManagerを注入してもらう
    private EntityManager em;

    // ② 保存（INSERT）
    public Day66Note save(Day66Note note) {
        em.persist(note);     // 「このEntityをDBに登録して」
        return note;          // persist後にIDが入る
    }

    // ③ 1件取得（SELECT ... WHERE id = ?）
    public Optional<Day66Note> findById(Long id) {
        Day66Note found = em.find(Day66Note.class, id);
        return Optional.ofNullable(found);  // null の可能性があるので Optional
    }

    // ④ 全件取得（SELECT ... ORDER BY id DESC）
    public List<Day66Note> findAll() {
        // JPQL：SQLに似ているがEntityを対象にしたクエリ言語
        return em.createQuery("select n from Day66Note n order by n.id desc", Day66Note.class)
                .getResultList();
    }
}