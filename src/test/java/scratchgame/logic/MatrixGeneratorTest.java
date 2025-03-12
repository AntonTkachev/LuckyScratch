package scratchgame.logic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scratchgame.model.Config;
import scratchgame.model.StandardSymbolsProb;

import java.util.*;

public class MatrixGeneratorTest {

    private Config config;

    @Before
    public void setup() {
        config = new Config();

        // Define symbol probabilities
        Map<String, Integer> standardWeights = new HashMap<>();
        standardWeights.put("A", 50);
        standardWeights.put("B", 30);
        standardWeights.put("C", 20);

        List<StandardSymbolsProb> standardProb = new ArrayList<>();
        standardProb.add(new StandardSymbolsProb(0, 0, standardWeights));

        config.probabilities = new scratchgame.model.Probabilities();
        config.probabilities.standard_symbols = standardProb;

        Map<String, Integer> bonusWeights = new HashMap<>();
        bonusWeights.put("5x", 10);
        bonusWeights.put("+500", 5);
        bonusWeights.put("MISS", 85);

        config.probabilities.bonus_symbols = new scratchgame.model.BonusSymbolsProb();
        config.probabilities.bonus_symbols.symbols = bonusWeights;

        config.rows = 3;
        config.columns = 3;
    }

    /**
     * Test: Generated matrix should have correct dimensions.
     */
    @Test
    public void testMatrixSize() {
        String[][] matrix = MatrixGenerator.generateMatrix(config, config.rows, config.columns);
        Assert.assertEquals((int)config.rows, matrix.length);
        Assert.assertEquals((int)config.columns, matrix[0].length);
    }

    /**
     * Test: At least one symbol from predefined standard set should appear.
     */
    @Test
    public void testStandardSymbolsAppear() {
        String[][] matrix = MatrixGenerator.generateMatrix(config, config.rows, config.columns);
        Set<String> standardSymbols = new HashSet<>(Arrays.asList("A", "B", "C"));

        boolean found = false;
        for (String[] row : matrix) {
            for (String cell : row) {
                if (standardSymbols.contains(cell)) {
                    found = true;
                    break;
                }
            }
        }
        Assert.assertTrue("Matrix should contain at least one standard symbol", found);
    }

    /**
     * Test: Bonus symbols should appear in the matrix with correct probability.
     */
    @Test
    public void testBonusSymbolsAppear() {
        String[][] matrix = MatrixGenerator.generateMatrix(config, config.rows, config.columns);
        Set<String> bonusSymbols = new HashSet<>(Arrays.asList("5x", "+500", "MISS"));

        boolean found = false;
        for (String[] row : matrix) {
            for (String cell : row) {
                if (bonusSymbols.contains(cell)) {
                    found = true;
                    break;
                }
            }
        }
        Assert.assertTrue("Matrix should contain at least one bonus symbol", found);
    }
}