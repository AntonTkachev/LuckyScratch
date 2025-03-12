package scratchgame.logic;

import scratchgame.model.Config;
import scratchgame.model.StandardSymbolsProb;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class MatrixGenerator {

    public static String[][] generateMatrix(Config config, int rows, int cols) {
        String[][] matrix = new String[rows][cols];
        Random rand = new Random();

        // Probability settings for standard symbols
        List<StandardSymbolsProb> stdProb = config.probabilities.standard_symbols;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Example: 15% chance to place a bonus symbol
                boolean isBonus = (rand.nextInt(100) < 15);

                if (!isBonus) {
                    // Get the probability settings for (r, c)
                    StandardSymbolsProb probForCell = findProbForCell(stdProb, r, c);
                    matrix[r][c] = pickSymbolByWeights(probForCell.symbols);
                } else {
                    // Pick from bonus symbols
                    matrix[r][c] = pickSymbolByWeights(config.probabilities.bonus_symbols.symbols);
                }
            }
        }
        return matrix;
    }

    /**
     * Finds probability settings for the given cell (row, col).
     * Uses the first list entry as a fallback if not found.
     */
    private static StandardSymbolsProb findProbForCell(List<StandardSymbolsProb> list, int row, int col) {
        for (StandardSymbolsProb s : list) {
            if (s.row == row && s.column == col) {
                return s;
            }
        }
        return list.get(0);
    }

    /**
     * Picks a symbol based on weighted probabilities.
     */
    private static String pickSymbolByWeights(Map<String, Integer> symbolWeights) {
        int total = symbolWeights.values().stream().mapToInt(i -> i).sum();
        int rnd = new Random().nextInt(total) + 1;
        int current = 0;
        for (Map.Entry<String, Integer> e : symbolWeights.entrySet()) {
            current += e.getValue();
            if (rnd <= current) {
                return e.getKey();
            }
        }
        // Theoretically never reached if total > 0
        return "MISS";
    }
}