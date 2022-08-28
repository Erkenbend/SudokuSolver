package improved;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Grid {
    private final Cell[][] content;
    private List<List<Cell>> tiedCellGroups = new ArrayList<>();
    private boolean changeTracker = false;
    private final List<Integer> history;

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
                int value = row[colIdx];
                if (value < 0 || value > 9) {
                    throw new IllegalArgumentException("Invalid value");
                }
                this.content[rowIdx][colIdx] = new Cell(rowIdx, colIdx, value);
            }
        }

        this.history = new ArrayList<>();
    }

    public Grid(Grid other) {
        Cell[][] content = new Cell[9][9];
        for (int rowIdx = 0; rowIdx < 9; rowIdx++) {
            for (int colIdx = 0; colIdx < 9; colIdx++) {
                content[rowIdx][colIdx] = new Cell(other.content[rowIdx][colIdx]);
            }
        }
        this.content = content;
        this.changeTracker = false;
        this.tiedCellGroups = new ArrayList<>();
        this.history = new ArrayList<>(other.history);
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

    public String getHistory() {
        StringBuilder sb = new StringBuilder();
        sb.append("%d guess%s needed\n".formatted(this.history.size() - 1, this.history.size() == 2 ? "" : "es"));
        for (int nbPasses : this.history) {
            sb.append("* %d pass%s\n".formatted(nbPasses, nbPasses == 1 ? "" : "es"));
        }
        return sb.toString();
    }

    public boolean isChanging() {
        boolean tmpChangeTracker = this.changeTracker;
        this.changeTracker = false;
        return tmpChangeTracker;
    }

    /**
     * @return true if grid is complete, false if stuck, throws if grid is invalid
     */
    public boolean simpleSolve() {
        int nbPasses = 0;
        boolean success = false;
        do {
            nbPasses++;
            this.performOnePass();

            if (this.isComplete()) {
                success = true;
                break;
            }

        } while (this.isChanging());

        this.history.add(nbPasses);
        return success;
    }

    public void performOnePass() {
        if (tiedCellGroups.isEmpty()) {
            this.computeTiedCellGroups();
        }

        this.tiedCellGroups.forEach(this::updateCandidates);
        this.convertLoneCandidates();
        this.tiedCellGroups.forEach(this::convertSingleStandingCandidates);
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

    public List<Grid> applyHypotheses() {
        Cell bestFreeCell = this.findBestFreeCell()
                .orElseThrow(() -> new IllegalArgumentException("No free cells"));

        List<Grid> grids = new ArrayList<>();
        Set<Integer> candidates = Set.copyOf(bestFreeCell.getCandidates());
        for (int candidate : candidates) {
            Grid hypothesisGrid = new Grid(this);
            hypothesisGrid.computeTiedCellGroups();
            int rowIdx = bestFreeCell.getRowIdx();
            int colIdx = bestFreeCell.getColIdx();
            // System.out.printf("Assuming value %d in cell (%d, %d)%n", candidate, rowIdx, colIdx);
            hypothesisGrid.content[rowIdx][colIdx].writeValue(candidate);
            grids.add(hypothesisGrid);
        }
        return grids;
    }

    private Optional<Cell> findBestFreeCell() {
        int minimalNbCandidates = 10;
        Cell bestFreeCell = null;
        for (int rowIdx = 0; rowIdx < 9; rowIdx++) {
            Cell[] row = this.content[rowIdx];
            for (int colIdx = 0; colIdx < 9; colIdx++) {
                Cell cell = row[colIdx];
                if (cell.getValue().isEmpty()) {
                    int nbCandidates = cell.getCandidates().size();
                    if (nbCandidates < minimalNbCandidates) {
                        bestFreeCell = cell;
                        minimalNbCandidates = nbCandidates;
                    }
                }
            }
        }
        return Optional.ofNullable(bestFreeCell);
    }

    private void computeTiedCellGroups() {
        IntStream.range(0, 9)
                .forEach(idx -> {
                    this.tiedCellGroups.add(this.getRow(idx));
                    this.tiedCellGroups.add(this.getCol(idx));
                    this.tiedCellGroups.add(this.getBlock(idx / 3, idx % 3));
                });

        tiedCellGroups.forEach(cellGroup -> {
            // check group validity
            List<Integer> valuesInGroup = cellGroup.stream()
                    .map(Cell::getValue)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

            if (valuesInGroup.size() != Set.copyOf(valuesInGroup).size()) {
                throw new IllegalArgumentException("Invalid grid, duplicate values in tied cells");
            }

            // add group info to each cell
            cellGroup.forEach(cell -> cell.addTiedCells(cellGroup));
        });
    }

    private void convertSingleStandingCandidates(List<Cell> tiedCells) {
        IntStream.rangeClosed(1, 9)
                .forEach(value -> {
                            List<Integer> candidatesIdxForValue = getCandidatesIdx(tiedCells, value);
                            if (candidatesIdxForValue.size() == 1) {
                                Cell candidate = tiedCells.get(candidatesIdxForValue.get(0));
                                if (candidate.getValue().isEmpty()) {
                                    candidate.writeValue(value);
                                    this.changeTracker = true;
                                }
                            }
                        }
                );
    }

    private List<Integer> getCandidatesIdx(List<Cell> tiedCells, int value) {
        List<Integer> candidatesIdxForValue = new ArrayList<>();

        for (int cellIdx = 0; cellIdx < 9; cellIdx++) {
            Cell cell = tiedCells.get(cellIdx);
            Optional<Integer> cellValue = cell.getValue();
            if ((cellValue.isPresent() && cellValue.get() == value)
                    || (cellValue.isEmpty() && cell.getCandidates().contains(value))
            ) {
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

    private List<Cell> getRow(int rowIdx) {
        return Arrays.stream(this.content[rowIdx])
                .toList();
    }

    private List<Cell> getCol(int colIdx) {
        return Arrays.stream(this.content)
                .map(row -> row[colIdx])
                .toList();
    }

    private List<Cell> getBlock(int blockRowIdx, int blockColIdx) {
        return IntStream.range(0, 9)
                .mapToObj(i -> this.content[3 * blockRowIdx + i / 3][3 * blockColIdx + i % 3])
                .toList();
    }

    private void updateCandidates(List<Cell> tiedCells) {
        Set<Integer> presentValues = tiedCells.stream()
                .map(Cell::getValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        List<Boolean> changesPerformed = tiedCells.stream()
                .filter(cell -> cell.getValue().isEmpty())
                .map(cell -> {
                    Set<Integer> candidates = cell.getCandidates();
                    boolean removedValues = candidates.removeAll(presentValues);
                    if (candidates.size() == 0) {
                        throw new IllegalArgumentException("Invalid grid");
                    }
                    return removedValues;
                })
                .toList();

        this.changeTracker = this.changeTracker || changesPerformed.stream().reduce(Boolean.FALSE, Boolean::logicalOr);

        // System.out.println(Arrays.toString(tiedCells));
    }
}
