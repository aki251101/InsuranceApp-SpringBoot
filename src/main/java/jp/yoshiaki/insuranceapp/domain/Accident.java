package jp.yoshiaki.insuranceapp.domain;

public class Accident {
    private final long id;
    private final String statusLabel;

    public Accident(long id, String statusLabel) {
        this.id = id;
        this.statusLabel = statusLabel;
    }

    public long getId() {
        return id;
    }

    public String getStatusLabel() {
        return statusLabel;
    }
}