package newsolver;

public class SudokuContradictionException extends Exception {

    private final int row;
    private final int col;

    public SudokuContradictionException(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public void printStackTrace() {
        System.err.printf("Contradiction in cell %d %d%n", row + 1, col + 1);
    }

}
