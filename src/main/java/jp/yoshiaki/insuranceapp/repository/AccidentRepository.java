package jp.yoshiaki.insuranceapp.repository;

import java.util.List;
import java.util.Optional;

import jp.yoshiaki.insuranceapp.domain.Accident;

public interface AccidentRepository {
    Optional<Accident> findById(Long id);
    List<Accident> findAll();
}
