package scratchgame.logic;

import scratchgame.model.CalculationResult;
import scratchgame.model.Config;
import scratchgame.model.SymbolConfig;
import scratchgame.model.WinCombination;

import java.util.*;

public class RewardCalculator {

    /**
     * Calculates winnings and stores a detailed breakdown in result.detailedBreakdown.
     */
    public static CalculationResult calculateReward(String[][] matrix, int bet, Config config) {
        CalculationResult result = new CalculationResult();

        if (matrix == null || config == null || config.symbols == null || matrix.length == 0 || matrix[0].length == 0) {
            result.finalReward = 0;
            result.appliedBonusSymbol = null;
            result.appliedCombinations = Collections.emptyMap();
            result.detailedBreakdown = Collections.singletonList("Invalid configuration or empty matrix provided.");
            return result;
        }

        double totalReward = 0.0;
        List<String> breakdown = new ArrayList<>();
        Map<String, Integer> symbolCount = new HashMap<>();

        int rows = matrix.length;
        int cols = matrix[0].length;

        // Count occurrences of each standard symbol
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String sym = matrix[r][c];
                SymbolConfig symCfg = config.symbols.get(sym);
                if (symCfg != null && "standard".equals(symCfg.type)) {
                    symbolCount.put(sym, symbolCount.getOrDefault(sym, 0) + 1);
                }
            }
        }

        Map<String, List<String>> appliedCombinations = new LinkedHashMap<>();

        // Check winning combinations for each symbol
        for (Map.Entry<String, Integer> entry : symbolCount.entrySet()) {
            String symbol = entry.getKey();
            int count = entry.getValue();
            SymbolConfig symCfg = config.symbols.get(symbol);

            double symbolBaseMultiplier = symCfg.reward_multiplier;
            double finalMultiplierForSymbol = 1.0;
            List<String> combosForSymbol = new ArrayList<>();

            // Check "same_symbols" combinations
            Optional<Map.Entry<String, WinCombination>> bestSameSymbolCombo =
                    config.win_combinations.entrySet().stream()
                            .filter(e -> "same_symbols".equals(e.getValue().when))
                            .filter(e -> e.getValue().count != null && count >= e.getValue().count)
                            .max(Comparator.comparingInt(e -> e.getValue().count));

            if (bestSameSymbolCombo.isPresent()) {
                WinCombination wc = bestSameSymbolCombo.get().getValue();
                finalMultiplierForSymbol *= wc.reward_multiplier;
                combosForSymbol.add(bestSameSymbolCombo.get().getKey());
            }

            // Check "linear_symbols" combinations (rows, columns, diagonals)
            Set<String> usedGroups = new HashSet<>();
            bestSameSymbolCombo.ifPresent(e -> usedGroups.add(e.getValue().group));

            for (Map.Entry<String, WinCombination> wcEntry : config.win_combinations.entrySet()) {
                WinCombination wc = wcEntry.getValue();
                if (!"linear_symbols".equals(wc.when)) {
                    continue;
                }
                if (usedGroups.contains(wc.group)) {
                    continue;
                }
                if (checkLinearCombo(matrix, symbol, wc.covered_areas)) {
                    finalMultiplierForSymbol *= wc.reward_multiplier;
                    combosForSymbol.add(wcEntry.getKey());
                    usedGroups.add(wc.group);
                }
            }

            if (combosForSymbol.isEmpty()) {
                continue;
            }

            // Calculate partial reward
            double partialReward = bet * symbolBaseMultiplier * finalMultiplierForSymbol;
            totalReward += partialReward;
            appliedCombinations.put(symbol, combosForSymbol);

            // Store breakdown of calculations
            breakdown.add(String.format(
                    "Symbol '%s': bet(%d) x symbol_multiplier(%.2f) x combo_multiplier(%.2f) = %.0f",
                    symbol, bet, symbolBaseMultiplier, finalMultiplierForSymbol, partialReward
            ));
        }

        breakdown.add(String.format("Total winnings before bonus: %.0f", totalReward));

        // Apply bonus if totalReward > 0
        String appliedBonusSymbol = null;
        if (totalReward > 0) {
            bonusLoop:
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    String sym = matrix[r][c];
                    SymbolConfig symCfg = config.symbols.get(sym);
                    if (symCfg != null && "bonus".equals(symCfg.type)) {
                        appliedBonusSymbol = sym;
                        switch (symCfg.impact) {
                            case "multiply_reward":
                                double oldValue = totalReward;
                                totalReward *= symCfg.reward_multiplier;
                                breakdown.add(String.format(
                                        "Bonus '%s' applied: (%.0f) x %.0f = %.0f",
                                        sym, oldValue, symCfg.reward_multiplier, totalReward
                                ));
                                break;
                            case "extra_bonus":
                                oldValue = totalReward;
                                totalReward += symCfg.extra;
                                breakdown.add(String.format(
                                        "Bonus '%s' applied: (%.0f) + %d = %.0f",
                                        sym, oldValue, symCfg.extra, totalReward
                                ));
                                break;
                            case "miss":
                                breakdown.add("Bonus 'MISS' (no effect).");
                                break;
                            default:
                                break;
                        }
                        break bonusLoop;
                    }
                }
            }
        }

        // Round final reward
        long finalReward = Math.round(totalReward);

        // Create result object
        result.finalReward = finalReward;
        result.appliedCombinations = appliedCombinations;
        result.appliedBonusSymbol = appliedBonusSymbol;
        result.detailedBreakdown = breakdown;

        return result;
    }

    /**
     * Checks if the given symbol fills at least one complete line from covered_areas.
     */
    private static boolean checkLinearCombo(String[][] matrix,
                                            String symbol,
                                            List<List<String>> coveredAreas) {
        if (coveredAreas == null) return false;

        for (List<String> line : coveredAreas) {
            boolean allMatch = true;
            for (String cell : line) {
                String[] parts = cell.split(":");
                int rr = Integer.parseInt(parts[0]);
                int cc = Integer.parseInt(parts[1]);
                if (!matrix[rr][cc].equals(symbol)) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) {
                return true;
            }
        }
        return false;
    }
}