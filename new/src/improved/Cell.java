package improved;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

public class Cell {
    private final int rowIdx;
    private final int colIdx;

    private Integer value;
    private Set<Integer> candidates;

    public Cell(int rowIdx, int colIdx, int value) {
        this.rowIdx = rowIdx;
        this.colIdx = colIdx;

        if (value == 0) {
            this.value = null;
            this.candidates = new HashSet<>(IntStream.rangeClosed(1, 9).boxed().toList());
        } else {
            this.value = value;
            this.candidates = new HashSet<>();
        }
    }

    public Cell(Cell other) {
        this.rowIdx = other.rowIdx;
        this.colIdx = other.colIdx;
        this.value = other.value;
        this.candidates = new HashSet<>(other.candidates);
    }

    public int getRowIdx() {
        return this.rowIdx;
    }

    public int getColIdx() {
        return this.colIdx;
    }

    public Optional<Integer> getValue() {
        return Optional.ofNullable(value);
    }

    public Set<Integer> getCandidates() {
        return candidates;
    }

    public void writeValue() {
        if (this.value != null) {
            throw new IllegalArgumentException("Value already present");
        }
        if (this.candidates.size() != 1) {
            throw new IllegalArgumentException("More than one candidate, cannot infer value");
        }
        this.value = this.candidates.iterator().next();
        this.candidates = Set.of();
    }

    public void writeValue(int value) {
        if (this.value != null) {
            throw new IllegalArgumentException("Value already present");
        }
        this.value = value;
        this.candidates = Set.of();
    }

    @Override
    public String toString() {
        if (this.value != null) {
            return "<%d>".formatted(this.value);
        }

        return "<?>(%s)".formatted(this.candidates);
    }
}
