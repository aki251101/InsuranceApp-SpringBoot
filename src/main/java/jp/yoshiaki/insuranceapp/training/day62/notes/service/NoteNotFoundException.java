package jp.yoshiaki.insuranceapp.training.day62.notes.service;

/**
 * 業務例外：指定IDのノートが見つからない
 */
public class NoteNotFoundException extends RuntimeException {
    private final long id;

    public NoteNotFoundException(long id) {
        super("ノートが見つかりません: id=" + id);
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
