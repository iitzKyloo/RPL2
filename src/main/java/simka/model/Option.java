package simka.model;

public record Option(int id, String label) {
    @Override public String toString() { return label; }
}
//Spring