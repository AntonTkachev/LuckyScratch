package scratchgame.logic;

import org.junit.Assert;
import org.junit.Test;
import scratchgame.model.CalculationResult;
import scratchgame.model.Config;
import scratchgame.model.SymbolConfig;
import scratchgame.model.WinCombination;

import java.util.*;

public class RewardCalculatorTest {

    private Config createTestConfig() {
        Config config = new Config();

        config.symbols = Map.of(
                "A", new SymbolConfig("standard", 5.0),
                "B", new SymbolConfig("standard", 3.0),
                "C", new SymbolConfig("standard", 2.5),
                "D", new SymbolConfig("standard", 2.0),
                "E", new SymbolConfig("standard", 1.2),
                "F", new SymbolConfig("standard", 1.0),
                "5x", new SymbolConfig("bonus", 5.0, "multiply_reward"),
                "+500", new SymbolConfig("bonus", 0, "extra_bonus", 500),
                "MISS", new SymbolConfig("bonus", 0, "miss")
        );

        config.win_combinations = Map.of(
                "same_symbol_3_times", new WinCombination("same_symbols", 3, 1.0, "same_symbols"),
                "same_symbol_4_times", new WinCombination("same_symbols", 4, 1.5, "same_symbols"),
                "same_symbols_horizontally", new WinCombination("linear_symbols", null, 2.0, "linear", List.of(List.of("0:0", "0:1", "0:2")))
        );

        return config;
    }

    @Test
    public void testNoWinningComboWithBonus() {
        Config config = createTestConfig();
        String[][] matrix = {{"5x", "A", "C"}, {"B", "D", "E"}, {"F", "A", "D"}};
        CalculationResult result = RewardCalculator.calculateReward(matrix, 100, config);

        Assert.assertEquals(0, result.finalReward);
        Assert.assertNull(result.appliedBonusSymbol);
    }

    @Test
    public void testFourSameSymbolsWithBonus() {
        Config config = createTestConfig();

        String[][] matrix = {
                {"F", "F", "A"},
                {"B", "F", "B"},
                {"F", "C", "5x"}
        };

        CalculationResult result = RewardCalculator.calculateReward(matrix, 200, config);

        Assert.assertEquals(1500, result.finalReward);
        Assert.assertEquals("5x", result.appliedBonusSymbol);
    }

    @Test
    public void testHorizontalLineCombination() {
        Config config = createTestConfig();

        String[][] matrix = {
                {"C", "C", "C"},
                {"A", "B", "E"},
                {"F", "D", "MISS"}
        };

        CalculationResult result = RewardCalculator.calculateReward(matrix, 100, config);

        Assert.assertEquals(500, result.finalReward);
        Assert.assertTrue(result.appliedCombinations.get("C").contains("same_symbols_horizontally"));
    }

    @Test
    public void testEmptyMatrix() {
        Config config = createTestConfig();

        String[][] matrix = new String[0][0];
        CalculationResult result = RewardCalculator.calculateReward(matrix, 100, config);

        Assert.assertEquals(0, result.finalReward);
        Assert.assertTrue(result.appliedCombinations.isEmpty());
        Assert.assertNull(result.appliedBonusSymbol);
    }

    @Test
    public void testNullMatrix() {
        Config config = createTestConfig();

        CalculationResult result = RewardCalculator.calculateReward(null, 100, config);

        Assert.assertEquals(0, result.finalReward);
        Assert.assertTrue(result.appliedCombinations.isEmpty());
        Assert.assertNull(result.appliedBonusSymbol);
    }

    @Test
    public void testNullConfig() {
        String[][] matrix = {{"A", "A", "A"}};

        CalculationResult result = RewardCalculator.calculateReward(matrix, 100, null);

        Assert.assertEquals(0, result.finalReward);
        Assert.assertTrue(result.appliedCombinations.isEmpty());
        Assert.assertNull(result.appliedBonusSymbol);
    }

    @Test
    public void testMatrixWithUnknownSymbols() {
        Config config = createTestConfig();

        String[][] matrix = {
                {"X", "Y", "Z"},
                {"X", "Y", "Z"},
                {"X", "Y", "Z"}
        };

        CalculationResult result = RewardCalculator.calculateReward(matrix, 100, config);

        Assert.assertEquals(0, result.finalReward);
        Assert.assertTrue(result.appliedCombinations.isEmpty());
        Assert.assertNull(result.appliedBonusSymbol);
    }
}