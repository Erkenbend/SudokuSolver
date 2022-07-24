package newsolver;

public class Operation {

    private final int row;
    private final int col;
    private final int val;
    private final Grid stateSaved;

    public Operation(int row, int col, int val, Grid stateSaved) {
        this.row = row;
        this.col = col;
        this.val = val;
        this.stateSaved = stateSaved;
    }

    public Grid getStateSaved() {
        return stateSaved;
    }

    public String toString() {
        return "> %d %d - %d".formatted(this.row, this.col, this.val);
    }

}
