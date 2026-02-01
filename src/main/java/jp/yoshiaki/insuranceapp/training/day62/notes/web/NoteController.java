package jp.yoshiaki.insuranceapp.training.day62.notes.web;

import jp.yoshiaki.insuranceapp.training.day62.notes.domain.Note;
import jp.yoshiaki.insuranceapp.training.day62.notes.service.NoteService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import jp.yoshiaki.insuranceapp.training.day62.notes.service.NoteNotFoundException;

/**
 * REST Controller（今日の主役）
 * - GETエンドポイントを作る
 * - ドメインをそのまま返さず、表示用DTO（Map）に変換して返す（学習用）
 */

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private final NoteService service;

    public NoteController(NoteService service) {
        this.service = service;
    }

    private Map<String, Object> toDto(Note n) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", n.getId());
        dto.put("title", n.getTitle());
        dto.put("status", n.getStatus().name());
        dto.put("statusLabel", n.getStatus().getLabelJa());
        dto.put("createdAt", n.getCreatedAt().toString());
        return dto;
    }

    /**
     * 一覧取得
     * 例: GET /api/notes
     * 例: GET /api/notes?status=DONE
     * 例: GET /api/notes?status=完了
     */
    @GetMapping
    public List<Map<String, Object>> list(@RequestParam(name = "status", required = false) String status) {
        try {
            return service.list(status).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException ex) {
            // status=XXX などの使用外の値 → 400
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    /**
     * 1件取得
     * 例: GET /api/notes/1
     */
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        try {
            Note n = service.getById(id);
            return toDto(n);
        } catch (NoteNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}
