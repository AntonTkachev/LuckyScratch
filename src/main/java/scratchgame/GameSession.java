package scratchgame;

import scratchgame.logic.MatrixGenerator;
import scratchgame.logic.RewardCalculator;
import scratchgame.model.CalculationResult;
import scratchgame.model.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GameSession {

    // Mapping internal combo names to more readable strings
    private static final Map<String, String> COMBO_READABLE_NAMES = new HashMap<>();

    static {
        COMBO_READABLE_NAMES.put("same_symbol_3_times", "3 identical symbols");
        COMBO_READABLE_NAMES.put("same_symbol_4_times", "4 identical symbols");
        COMBO_READABLE_NAMES.put("same_symbol_5_times", "5 identical symbols");
        COMBO_READABLE_NAMES.put("same_symbol_6_times", "6 identical symbols");
        COMBO_READABLE_NAMES.put("same_symbol_7_times", "7 identical symbols");
        COMBO_READABLE_NAMES.put("same_symbol_8_times", "8 identical symbols");
        COMBO_READABLE_NAMES.put("same_symbol_9_times", "9 identical symbols");
        COMBO_READABLE_NAMES.put("same_symbols_horizontally", "horizontal line");
        COMBO_READABLE_NAMES.put("same_symbols_vertically", "vertical line");
        COMBO_READABLE_NAMES.put("same_symbols_diagonally_left_to_right", "diagonal (left-to-right)");
        COMBO_READABLE_NAMES.put("same_symbols_diagonally_right_to_left", "diagonal (right-to-left)");
    }

    public static void playRound(Scanner scanner, Config config) {
        System.out.print("\nEnter your bet (default 100): ");
        String input = scanner.nextLine().trim();
        int betAmount = 100;
        if (!input.isEmpty()) {
            try {
                betAmount = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, using default bet: 100.");
            }
        }

        int rows = (config.rows == null) ? 3 : config.rows;
        int cols = (config.columns == null) ? 3 : config.columns;

        // Generate the matrix
        String[][] matrix = MatrixGenerator.generateMatrix(config, rows, cols);

        // Calculate the reward
        CalculationResult result = RewardCalculator.calculateReward(matrix, betAmount, config);

        // Print the outcome
        printGameResult(matrix, result);
    }

    private static void printGameResult(String[][] matrix, CalculationResult result) {
        System.out.println("\nGenerated matrix:");
        for (String[] row : matrix) {
            for (String cell : row) {
                System.out.printf("%-6s ", cell);
            }
            System.out.println();
        }

        if (result.appliedCombinations == null || result.appliedCombinations.isEmpty()) {
            System.out.println("\nNo winning combinations (you lost).");
        } else {
            System.out.println("\nWinning combinations:");
            result.appliedCombinations.forEach((symbol, combos) -> {
                for (String combo : combos) {
                    String readable = COMBO_READABLE_NAMES.getOrDefault(combo, combo);
                    System.out.println("  Symbol '" + symbol + "': " + readable);
                }
            });
        }

        System.out.println("\nDetailed breakdown:");
        if (result.detailedBreakdown != null && !result.detailedBreakdown.isEmpty()) {
            for (String line : result.detailedBreakdown) {
                System.out.println("  " + line);
            }
        } else {
            System.out.println("  No details (no winnings?).");
        }

        if (result.appliedBonusSymbol == null) {
            System.out.println("\nNo bonus applied.");
        } else if ("MISS".equals(result.appliedBonusSymbol)) {
            System.out.println("\nBonus: MISS (no effect).");
        } else {
            System.out.println("\nBonus applied: " + result.appliedBonusSymbol);
        }

        System.out.println("\nFinal reward: " + result.finalReward);
    }
}