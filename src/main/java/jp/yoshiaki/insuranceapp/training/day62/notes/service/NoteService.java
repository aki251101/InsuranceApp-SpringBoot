package jp.yoshiaki.insuranceapp.training.day62.notes.service;

import jp.yoshiaki.insuranceapp.training.day62.notes.domain.Note;
import jp.yoshiaki.insuranceapp.training.day62.notes.domain.NoteStatus;
import jp.yoshiaki.insuranceapp.training.day62.notes.repository.NoteRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service（業務操作）
 */
@Profile("training")
@Service
public class NoteService {
    private final NoteRepository repository;

    public NoteService(NoteRepository repository) {
        this.repository = repository;
    }

    public List<Note> list(String statusRawOrNull) {
        List<Note> all = repository.findAll().stream()
                .sorted(Comparator.comparingLong(Note::getId))
                .collect(Collectors.toList());

        if (statusRawOrNull == null || statusRawOrNull.trim().isEmpty()) {
            return all;
        }

        NoteStatus status = NoteStatus.parse(statusRawOrNull);
        return all.stream()
                .filter(n -> n.getStatus() == status)
                .collect(Collectors.toList());
    }

    public Note getById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }
}
