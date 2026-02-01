package jp.yoshiaki.insuranceapp.training.day62.notes.domain;

import java.time.LocalDateTime;

/**
 * ドメイン：ノート
 * - ルール（状態遷移）を自分で持つ
 */
public class Note {
    private final long id;
    private final String title;
    private NoteStatus status;
    private final LocalDateTime createdAt;

    public Note(long id, String title, NoteStatus status) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public NoteStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void start() {
        if (status == NoteStatus.DONE) {
            throw new IllegalStateException("完了したノートは開始できません");
        }
        this.status = NoteStatus.DOING;
    }

    public void complete() {
        this.status = NoteStatus.DONE;
    }
}
