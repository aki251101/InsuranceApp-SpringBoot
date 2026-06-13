package jp.yoshiaki.insuranceapp.repository;

import jp.yoshiaki.insuranceapp.entity.AccidentMemo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccidentMemoRepository extends JpaRepository<AccidentMemo, Long> {

    List<AccidentMemo> findByAccidentIdOrderByHandledAtDescIdDesc(Long accidentId);

    Optional<AccidentMemo> findByIdAndAccidentId(Long id, Long accidentId);
}
