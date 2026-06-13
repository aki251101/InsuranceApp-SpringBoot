package jp.yoshiaki.insuranceapp.service;

import jp.yoshiaki.insuranceapp.entity.Accident;
import jp.yoshiaki.insuranceapp.entity.AccidentMemo;
import jp.yoshiaki.insuranceapp.repository.AccidentMemoRepository;
import jp.yoshiaki.insuranceapp.repository.AccidentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccidentServiceTest {

    @Mock
    private AccidentRepository accidentRepository;

    @Mock
    private AccidentMemoRepository accidentMemoRepository;

    @Test
    void addMemoStoresOneEntryAndUpdatesLastContactedAtFromLatestMarkedEntry() {
        Accident accident = accident(6L, "IN_PROGRESS");
        LocalDateTime handledAt = LocalDateTime.of(2026, 6, 12, 12, 51);
        AccidentMemo existing = memo(1L, handledAt.minusDays(1), true);

        when(accidentRepository.findByIdWithPolicy(6L)).thenReturn(Optional.of(accident));
        when(accidentMemoRepository.save(any(AccidentMemo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(accidentMemoRepository.findByAccidentIdOrderByHandledAtDescIdDesc(6L))
                .thenAnswer(invocation -> List.of(
                        memo(2L, handledAt, true),
                        existing));

        AccidentService service = new AccidentService(accidentRepository, accidentMemoRepository);
        service.addMemo(6L, handledAt, "修理工場へ進捗確認", "tester", true);

        ArgumentCaptor<AccidentMemo> memoCaptor = ArgumentCaptor.forClass(AccidentMemo.class);
        verify(accidentMemoRepository).save(memoCaptor.capture());
        assertThat(memoCaptor.getValue().getContent()).isEqualTo("修理工場へ進捗確認");
        assertThat(memoCaptor.getValue().isUpdatesLastContacted()).isTrue();
        assertThat(accident.getLastContactedAt()).isEqualTo(handledAt);
        verify(accidentRepository).save(accident);
    }

    @Test
    void updateMemoRejectsMemoBelongingToAnotherAccident() {
        Accident accident = accident(6L, "IN_PROGRESS");
        when(accidentRepository.findByIdWithPolicy(6L)).thenReturn(Optional.of(accident));
        when(accidentMemoRepository.findByIdAndAccidentId(99L, 6L))
                .thenReturn(Optional.empty());

        AccidentService service = new AccidentService(accidentRepository, accidentMemoRepository);

        assertThatThrownBy(() -> service.updateMemoEntry(
                6L,
                99L,
                LocalDateTime.of(2026, 6, 12, 12, 51),
                "修正内容"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("対応履歴が見つかりません");
    }

    @Test
    void addMemoRejectsBlankContent() {
        Accident accident = accident(6L, "IN_PROGRESS");
        when(accidentRepository.findByIdWithPolicy(6L)).thenReturn(Optional.of(accident));

        AccidentService service = new AccidentService(accidentRepository, accidentMemoRepository);

        assertThatThrownBy(() -> service.addMemo(
                6L,
                LocalDateTime.of(2026, 6, 12, 12, 51),
                " ",
                "tester",
                false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("対応内容を入力してください");
    }

    private Accident accident(Long id, String status) {
        return Accident.builder()
                .id(id)
                .policyId(1L)
                .occurredAt(LocalDate.of(2026, 5, 17))
                .status(status)
                .memo("")
                .build();
    }

    private AccidentMemo memo(Long id, LocalDateTime handledAt, boolean updatesLastContacted) {
        return AccidentMemo.builder()
                .id(id)
                .accidentId(6L)
                .handledAt(handledAt)
                .content("対応内容")
                .createdBy("tester")
                .updatesLastContacted(updatesLastContacted)
                .build();
    }
}
