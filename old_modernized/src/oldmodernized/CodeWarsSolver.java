package oldmodernized;

public class CodeWarsSolver {
    private final Grid g;
    private final long startTime;

    public CodeWarsSolver(int[][] grid) {
        try {
            this.startTime = System.nanoTime();
            this.g = new Grid(grid);
            System.out.println("Original grid\n");
            System.out.print(this.g);
        } catch (InvalidGridException e) {
            throw new IllegalArgumentException();
        }
    }

    public int[][] solve() {
        Grid.validSolutions.clear();
        try {
            g.backtrackingSolve();
            if (Grid.validSolutions.size() != 1) {
                throw new IllegalArgumentException("More than one solution");
            }
            return Grid.validSolutions.get(0).toIntArray();
        } catch (GridFinishedException e) {
            long endTime = System.nanoTime();
            System.out.printf("Took %s ms%n", ((double) endTime - this.startTime) / Math.pow(10, 6));
            return Grid.validSolutions.get(0).toIntArray();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
