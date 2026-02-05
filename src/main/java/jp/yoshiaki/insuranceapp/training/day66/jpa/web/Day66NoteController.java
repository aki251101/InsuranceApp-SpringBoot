package jp.yoshiaki.insuranceapp.training.day66.jpa.web;

import jp.yoshiaki.insuranceapp.training.day66.jpa.domain.Day66Note;
import jp.yoshiaki.insuranceapp.training.day66.jpa.service.Day66NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("day66NoteController")
@RequestMapping("/training/day66/notes")
public class Day66NoteController {

    private final Day66NoteService service;

    public Day66NoteController(Day66NoteService service) {
        this.service = service;
    }

    // DTO（リクエスト用）
    public record CreateRequest(String title, String body) {}

    // DTO（レスポンス用）
    public record NoteResponse(Long id, String title, String body) {
        static NoteResponse from(Day66Note n) {
            return new NoteResponse(n.getId(), n.getTitle(), n.getBody());
        }
    }

    // POST /training/day66/notes
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // 成功時は201を返す
    public NoteResponse create(@RequestBody CreateRequest req) {
        // 最小限の入力チェック
        if (req == null || req.title() == null || req.title().isBlank()
                || req.body() == null || req.body().isBlank()) {
            throw new IllegalArgumentException("title/body required");
        }
        Day66Note created = service.create(req.title(), req.body());
        return NoteResponse.from(created);
    }

    // GET /training/day66/notes
    @GetMapping
    public List<NoteResponse> list() {
        return service.list().stream()
                .map(NoteResponse::from)
                .toList();
    }

    // GET /training/day66/notes/{id}
    @GetMapping("/{id}")
    public NoteResponse get(@PathVariable Long id) {
        try {
            return NoteResponse.from(service.getOrThrow(id));
        } catch (IllegalArgumentException e) {
            // 見つからない場合は404を返す
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}