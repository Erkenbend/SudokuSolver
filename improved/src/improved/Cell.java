package improved;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

public class Cell {
    private final int rowIdx;
    private final int colIdx;

    private Integer value;
    private Set<Integer> candidates;
    private Set<Cell> tiedCells = new HashSet<>();

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
        // cannot deep-copy tied cells because of infinite cross-references, need to recompute
        this.tiedCells = new HashSet<>();
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
        if (this.candidates.size() != 1) {
            throw new IllegalArgumentException("More than one candidate, cannot infer value");
        }

        this.writeValue(this.candidates.iterator().next());
    }

    public void addTiedCells(List<Cell> tiedCells) {
        this.tiedCells.addAll(tiedCells);
    }

    public void writeValue(int value) {
        if (this.value != null) {
            throw new IllegalArgumentException("Value already present");
        }
        this.value = value;
        this.candidates = Set.of();
        this.tiedCells.stream()
                .filter(cell -> cell.getValue().isEmpty())
                .forEach(cell -> cell.candidates.remove(value));
    }

    @Override
    public String toString() {
        if (this.value != null) {
            return "<%d>".formatted(this.value);
        }

        return "<?>(%s)".formatted(this.candidates);
    }
}
