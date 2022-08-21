package improved;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Grid {
    private final Cell[][] content;
    private final List<Cell[]> tiedCellGroups = new ArrayList<>();
    private boolean changeTracker = false;

    public Grid(int[][] rawContent) {
        if (rawContent == null) {
            throw new IllegalArgumentException("Input content cannot be null");
        }
        if (rawContent.length != 9) {
            throw new IllegalArgumentException("Invalid dimensions");
        }

        this.content = new Cell[9][9];

        for (int rowIdx = 0; rowIdx < 9; rowIdx++) {
            int[] row = rawContent[rowIdx];
            if (row.length != 9) {
                throw new IllegalArgumentException("Invalid dimensions");
            }
            for (int colIdx = 0; colIdx < 9; colIdx++) {
                this.content[rowIdx][colIdx] = new Cell(row[colIdx]);
            }
        }
    }

    public int[][] toIntMatrix() {
        int[][] rawContent = new int[9][9];
        for (int rowIdx = 0; rowIdx < 9; rowIdx++) {
            Cell[] row = this.content[rowIdx];
            for (int colIdx = 0; colIdx < 9; colIdx++) {
                Cell cell = row[colIdx];
                rawContent[rowIdx][colIdx] = cell.getValue().orElse(0);
            }
        }
        return rawContent;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int rowIdx = 0; rowIdx < 9; rowIdx++) {
            Cell[] row = this.content[rowIdx];
            for (int colIdx = 0; colIdx < 9; colIdx++) {
                Cell cell = row[colIdx];
                stringBuilder.append(cell.getValue().map(Object::toString).orElse(" "));
                if (colIdx % 3 == 2) {
                    stringBuilder.append("  ");
                }
            }
            stringBuilder.append("\n");
            if (rowIdx % 3 == 2) {
                stringBuilder.append("\n");
            }
        }

        return stringBuilder.toString();
    }

    public boolean isChanging() {
        boolean tmpChangeTracker = this.changeTracker;
        this.changeTracker = false;
        return tmpChangeTracker;
    }

    public void performOnePass() {
        if (tiedCellGroups.isEmpty()) {
            this.computeTiedCellGroups();
        }

        this.tiedCellGroups.forEach(this::updateCandidates);
        this.convertLoneCandidates();
        //this.tiedCellGroups.forEach(this::convertSingleStandingCandidates);
    }

    public boolean isComplete() {
        for (Cell[] row : this.content) {
            for (Cell cell : row) {
                if (cell.getValue().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void computeTiedCellGroups() {
        IntStream.range(0, 9)
                .forEach(idx -> {
                    this.tiedCellGroups.add(this.getRow(idx));
                    this.tiedCellGroups.add(this.getCol(idx));
                    this.tiedCellGroups.add(this.getBlock(idx / 3, idx % 3));
                });
    }

    private void convertSingleStandingCandidates(Cell[] tiedCells) {
        IntStream.rangeClosed(1, 9)
                .forEach(value -> {
                            List<Integer> candidatesIdxForValue = getCandidatesIdx(tiedCells, value);
                            if (candidatesIdxForValue.size() == 1) {
                                tiedCells[candidatesIdxForValue.get(0)].writeValue(value);
                            }
                        }
                );
    }

    private List<Integer> getCandidatesIdx(Cell[] tiedCells, int value) {
        List<Integer> candidatesIdxForValue = new ArrayList<>();

        for (int cellIdx = 0; cellIdx < 9; cellIdx++) {
            Cell cell = tiedCells[cellIdx];
            if (cell.getValue().isEmpty() && cell.getCandidates().contains(value)) {
                candidatesIdxForValue.add(cellIdx);
            }
        }
        return candidatesIdxForValue;
    }

    private void convertLoneCandidates() {
        for (Cell[] row : this.content) {
            for (Cell cell : row) {
                if (cell.getCandidates().size() == 1) {
                    cell.writeValue();
                    this.changeTracker = true;
                }
            }
        }
    }

    private Cell[] getRow(int rowIdx) {
        return this.content[rowIdx];
    }

    private Cell[] getCol(int colIdx) {
        return Arrays.stream(this.content)
                .map(row -> row[colIdx])
                .toList()
                .toArray(new Cell[9]);
    }

    private Cell[] getBlock(int blockRowIdx, int blockColIdx) {
        return IntStream.range(0, 9)
                .mapToObj(i -> this.content[3 * blockRowIdx + i / 3][3 * blockColIdx + i % 3])
                .toList()
                .toArray(new Cell[9]);
    }

    private void updateCandidates(Cell[] tiedCells) {
        Set<Integer> presentValues = Arrays.stream(tiedCells)
                .map(Cell::getValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        List<Boolean> changesPerformed = Arrays.stream(tiedCells)
                .filter(cell -> cell.getValue().isEmpty())
                .map(cell -> cell.getCandidates().removeAll(presentValues))
                .toList();

        this.changeTracker = this.changeTracker || changesPerformed.stream().reduce(Boolean.FALSE, Boolean::logicalOr);

        // System.out.println(Arrays.toString(tiedCells));
    }
}
