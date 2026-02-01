package jp.yoshiaki.insuranceapp.training.day62.notes.repository;

import jp.yoshiaki.insuranceapp.training.day62.notes.domain.Note;

import java.util.List;
import java.util.Optional;

/**
 * Repository（保存の窓口）
 * - Controller/Service からは interface に依存し、実装（InMemory等）を隠す
 */
public interface NoteRepository {
    List<Note> findAll();

    Optional<Note> findById(long id);

    Note save(Note note);
}
