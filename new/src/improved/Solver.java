package improved;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Solver {
    public static void main(String[] args) {
        Instant startTime = Instant.now();

        Grid grid = new Grid(GridExamples.GRID_4);
        System.out.println("Original:");
        System.out.println(grid);

        Grid resultGrid = backtrackingSolve(grid);
        boolean isComplete = resultGrid != null && resultGrid.isComplete();
//        boolean isComplete = grid.simpleSolve();

        System.out.println("Final:");
        System.out.println(resultGrid);

        if (isComplete) {
            System.out.printf("Done in %dms%n", Duration.between(startTime, Instant.now()).toMillis());
        } else {
            System.out.printf("Stuck after %dms%n", Duration.between(startTime, Instant.now()).toMillis());
        }
    }

    private static Grid backtrackingSolve(Grid grid) {
        try {
            if (grid.simpleSolve()) {
                return grid;
            }
        } catch (IllegalArgumentException e) {
            return null;
        }

        List<Grid> possibleGrids = grid.applyHypotheses();
        for (Grid possibleGrid : possibleGrids) {
            Grid resultGrid = backtrackingSolve(possibleGrid);
            if (resultGrid != null) {
                return resultGrid;
            }
        }

        return null;
    }
}
