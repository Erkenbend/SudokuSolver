package improved;

public class CellWithPosition {
    private final int rowIdx;
    private final int colIdx;
    private final Cell target;

    private CellWithPosition(int rowIdx, int colIdx, Cell target) {
        this.rowIdx = rowIdx;
        this.colIdx = colIdx;
        this.target = target;
    }

    public static CellWithPosition of(int row, int col, Cell target) {
        return new CellWithPosition(row, col, target);
    }

    public int getRowIdx() {
        return rowIdx;
    }

    public int getColIdx() {
        return colIdx;
    }

    public Cell getTarget() {
        return target;
    }
}
