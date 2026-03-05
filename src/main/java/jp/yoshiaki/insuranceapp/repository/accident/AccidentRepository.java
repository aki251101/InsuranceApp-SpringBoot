package jp.yoshiaki.insuranceapp.repository.accident;

import java.util.List;
import java.util.Optional;

import jp.yoshiaki.insuranceapp.domain.accident.Accident;

public interface AccidentRepository {
    Optional<Accident> findById(Long id);
    List<Accident> findAll();
}
