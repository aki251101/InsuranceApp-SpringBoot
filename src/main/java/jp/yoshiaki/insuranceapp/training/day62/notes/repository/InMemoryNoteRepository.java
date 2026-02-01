package jp.yoshiaki.insuranceapp.training.day62.notes.repository;

import jp.yoshiaki.insuranceapp.training.day62.notes.domain.Note;
import jp.yoshiaki.insuranceapp.training.day62.notes.domain.NoteStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * InMemoryのRepository実装（学習用）
 */
@Repository
public class InMemoryNoteRepository implements NoteRepository {
    private final ConcurrentHashMap<Long, Note> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    public InMemoryNoteRepository() {
        // デモ用の初期データ
        save(new Note(nextId(), "Day62: @RestController を理解する", NoteStatus.TODO));
        save(new Note(nextId(), "GET /api/notes を叩く", NoteStatus.DOING));
        save(new Note(nextId(), "パス不一致404の原因特定ドリル", NoteStatus.DONE));
    }

    private long nextId() {
        return seq.incrementAndGet();
    }

    @Override
    public List<Note> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Note> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Note save(Note note) {
        store.put(note.getId(), note);
        return note;
    }
}
