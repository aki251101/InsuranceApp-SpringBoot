package jp.yoshiaki.insuranceapp.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jp.yoshiaki.insuranceapp.domain.accident.Accident;

@Repository
public class InMemoryAccidentRepository implements AccidentRepository {

    private final Map<Long, Accident> store = new HashMap<>();

    public InMemoryAccidentRepository() {
        store.put(1L, new Accident(1L, "受付"));
        store.put(2L, new Accident(2L, "対応中"));
        store.put(3L, new Accident(3L, "クローズ"));
    }

    @Override
    public Optional<Accident> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Accident> findAll() {
        return new ArrayList<>(store.values());
    }
}
