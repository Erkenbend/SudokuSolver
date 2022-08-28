package improved;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Solver {
    public static void main(String[] args) {
        Instant startTime = Instant.now();

        Grid grid = new Grid(GridExamples.CW_GRID_5);
        System.out.println("Original:");
        System.out.println(grid);
        //System.out.println(Arrays.deepToString(grid.toIntMatrix()));

        List<Grid> resultGrids = backtrackingSolve(grid, 5, false);

        for (int i = 0; i < resultGrids.size(); i++) {
            Grid resultGrid = resultGrids.get(i);
            System.out.printf("Result %d:%n", i);
            System.out.println(resultGrid);
            System.out.println(resultGrid.getHistory());
        }

        if (!resultGrids.isEmpty()) {
            System.out.printf("Done in %dms%n", Duration.between(startTime, Instant.now()).toMillis());
        } else {
            System.out.printf("Stuck after %dms%n", Duration.between(startTime, Instant.now()).toMillis());
        }
    }

    private static List<Grid> backtrackingSolve(final Grid grid, int maxSolutions, final boolean strict) {
        try {
            if (grid.simpleSolve()) {
                return List.of(grid);
            }
        } catch (IllegalArgumentException e) {
            return List.of();
        }

        List<Grid> solutions = new ArrayList<>();
        List<Grid> possibleGrids = grid.applyHypotheses();
        for (Grid possibleGrid : possibleGrids) {
            List<Grid> resultGrids = backtrackingSolve(possibleGrid, maxSolutions, strict);
            solutions.addAll(resultGrids);
            if (solutions.size() > maxSolutions) {
                if (strict) {
                    throw new IllegalArgumentException("Too many solutions");
                }
                return solutions;
            }
        }

        return solutions;
    }
}
