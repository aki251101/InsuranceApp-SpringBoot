package jp.yoshiaki.insuranceapp.dto;

import jp.yoshiaki.insuranceapp.entity.AccidentMemo;
import lombok.Builder;
import lombok.Value;

import java.time.format.DateTimeFormatter;

@Value
@Builder
public class AccidentMemoResponse {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private static final DateTimeFormatter INPUT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    Long id;
    String handledAt;
    String handledAtInput;
    String content;
    String createdBy;

    public static AccidentMemoResponse from(AccidentMemo memo) {
        return AccidentMemoResponse.builder()
                .id(memo.getId())
                .handledAt(memo.getHandledAt().format(DISPLAY_FORMATTER))
                .handledAtInput(memo.getHandledAt().format(INPUT_FORMATTER))
                .content(memo.getContent())
                .createdBy(memo.getCreatedBy())
                .build();
    }
}
