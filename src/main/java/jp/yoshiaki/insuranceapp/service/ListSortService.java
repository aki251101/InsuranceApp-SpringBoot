package jp.yoshiaki.insuranceapp.service;

import jp.yoshiaki.insuranceapp.entity.Accident;
import jp.yoshiaki.insuranceapp.entity.Policy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 一覧画面のソート条件を管理する。
 */
@Service
public class ListSortService {

    private static final Set<String> ACCIDENT_SORT_KEYS = Set.of(
            "id", "occurredAt", "policyNumber", "customerName", "status", "stagnant");
    private static final Set<String> POLICY_SORT_KEYS = Set.of(
            "id", "policyNumber", "customerName", "startDate", "endDate",
            "status", "attentionRequired", "calendarRegistered");

    private static final Comparator<String> TEXT_COMPARATOR =
            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER);

    public List<Accident> sortAccidents(
            List<Accident> accidents, String sort, String direction) {

        String normalizedSort = normalizeAccidentSort(sort);
        Comparator<Accident> primary = accidentPrimaryComparator(normalizedSort);
        if (isDescending(direction)) {
            primary = primary.reversed();
        }

        Comparator<Accident> secondary = "occurredAt".equals(normalizedSort)
                ? Comparator.comparing(this::policyNumber, TEXT_COMPARATOR)
                : Comparator.comparing(
                        Accident::getOccurredAt, Comparator.nullsLast(Comparator.naturalOrder()));

        List<Accident> sorted = new ArrayList<>(accidents);
        sorted.sort(primary
                .thenComparing(secondary)
                .thenComparing(
                        Accident::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        return sorted;
    }

    public List<Policy> sortPolicies(
            List<Policy> policies, String sort, String direction) {

        String normalizedSort = normalizePolicySort(sort);
        Comparator<Policy> primary = policyPrimaryComparator(normalizedSort);
        if (isDescending(direction)) {
            primary = primary.reversed();
        }

        Comparator<Policy> secondary = "endDate".equals(normalizedSort)
                ? Comparator.comparing(Policy::getPolicyNumber, TEXT_COMPARATOR)
                : Comparator.comparing(
                        Policy::getEndDate, Comparator.nullsLast(Comparator.naturalOrder()));

        List<Policy> sorted = new ArrayList<>(policies);
        sorted.sort(primary
                .thenComparing(secondary)
                .thenComparing(
                        Policy::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        return sorted;
    }

    public String normalizeAccidentSort(String sort) {
        return ACCIDENT_SORT_KEYS.contains(sort) ? sort : "occurredAt";
    }

    public String normalizePolicySort(String sort) {
        return POLICY_SORT_KEYS.contains(sort) ? sort : "endDate";
    }

    public String normalizeDirection(String direction) {
        return isDescending(direction) ? "desc" : "asc";
    }

    private Comparator<Accident> accidentPrimaryComparator(String sort) {
        return switch (sort) {
            case "id" -> Comparator.comparing(
                    Accident::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case "policyNumber" -> Comparator.comparing(this::policyNumber, TEXT_COMPARATOR);
            case "customerName" -> Comparator.comparing(this::customerName, TEXT_COMPARATOR);
            case "status" -> Comparator.comparingInt(
                    accident -> accidentStatusRank(accident.getStatus()));
            case "stagnant" -> Comparator.comparing(Accident::isStagnant).reversed();
            default -> Comparator.comparing(
                    Accident::getOccurredAt, Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }

    private Comparator<Policy> policyPrimaryComparator(String sort) {
        return switch (sort) {
            case "id" -> Comparator.comparing(
                    Policy::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case "policyNumber" ->
                    Comparator.comparing(Policy::getPolicyNumber, TEXT_COMPARATOR);
            case "customerName" ->
                    Comparator.comparing(Policy::getCustomerName, TEXT_COMPARATOR);
            case "startDate" -> Comparator.comparing(
                    Policy::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "status" ->
                    Comparator.comparingInt(policy -> policyStatusRank(policy.getEffectiveStatus()));
            case "attentionRequired" ->
                    Comparator.comparing(Policy::isAttentionRequired).reversed();
            case "calendarRegistered" -> Comparator.comparing(
                    policy -> Boolean.TRUE.equals(policy.getCalendarRegistered()),
                    Comparator.reverseOrder());
            default -> Comparator.comparing(
                    Policy::getEndDate, Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }

    private String policyNumber(Accident accident) {
        return policyValue(accident, Policy::getPolicyNumber);
    }

    private String customerName(Accident accident) {
        return policyValue(accident, Policy::getCustomerName);
    }

    private String policyValue(Accident accident, Function<Policy, String> getter) {
        return accident.getPolicy() != null ? getter.apply(accident.getPolicy()) : null;
    }

    private int accidentStatusRank(String status) {
        return switch (status) {
            case "OPEN" -> 0;
            case "IN_PROGRESS" -> 1;
            case "RESOLVED" -> 2;
            default -> 3;
        };
    }

    private int policyStatusRank(String status) {
        return switch (status) {
            case "契約中" -> 0;
            case "失効" -> 1;
            case "解約" -> 2;
            default -> 3;
        };
    }

    private boolean isDescending(String direction) {
        return "desc".equalsIgnoreCase(direction);
    }
}
