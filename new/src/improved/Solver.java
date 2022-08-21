package improved;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

public class Solver {
    public static void main(String[] args) {
        Instant startTime = Instant.now();

        Grid grid = new Grid(GridExamples.CW_GRID_4);
        //System.out.println(Arrays.deepToString(grid.toIntMatrix()));
        System.out.println("Original:");
        System.out.println(grid);
        int nbPasses = 0;

        do {
            grid.performOnePass();
            System.out.printf("Pass %d:%n", ++nbPasses);
            System.out.println(grid);

            if (grid.isComplete()) {
                System.out.printf("Done in %d passes and %dms%n", nbPasses, Duration.between(startTime, Instant.now()).toMillis());
                return;
            }

        } while (grid.isChanging());

        System.out.printf("Stuck after %d passes and %dms%n", nbPasses, Duration.between(startTime, Instant.now()).toMillis());
    }
}
