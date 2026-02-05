package jp.yoshiaki.insuranceapp.training.day66.jpa.service;

import jp.yoshiaki.insuranceapp.training.day66.jpa.domain.Day66Note;
import jp.yoshiaki.insuranceapp.training.day66.jpa.repository.Day66NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("day66NoteService")
public class Day66NoteService {

    private final Day66NoteRepository repo;

    // コンストラクタインジェクション
    public Day66NoteService(Day66NoteRepository repo) {
        this.repo = repo;
    }

    @Transactional  // ① 「このメソッドはトランザクション内で実行」
    public Day66Note create(String title, String body) {
        Day66Note note = new Day66Note(title, body);
        return repo.save(note);
    }

    @Transactional(readOnly = true)  // ② 読み取り専用（最適化のヒント）
    public List<Day66Note> list() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Day66Note getOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: id=" + id));
    }
}
