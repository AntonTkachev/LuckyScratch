package scratchgame;

import org.junit.Before;
import org.junit.Test;
import scratchgame.model.Config;
import scratchgame.model.StandardSymbolsProb;
import scratchgame.model.SymbolConfig;
import scratchgame.model.WinCombination;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GameSessionTest {

    private Config config;

    @Before
    public void setup() {
        config = new Config();
        config.rows = 3;
        config.columns = 3;

        // Initialize probabilities
        config.probabilities = new scratchgame.model.Probabilities();

        // Standard symbols with probabilities
        Map<String, Integer> standardWeights = new HashMap<>();
        standardWeights.put("A", 30);
        standardWeights.put("B", 30);
        standardWeights.put("C", 40);

        StandardSymbolsProb stdProb = new StandardSymbolsProb();
        stdProb.row = 0;
        stdProb.column = 0;
        stdProb.symbols = standardWeights;

        config.probabilities.standard_symbols = new ArrayList<>();
        config.probabilities.standard_symbols.add(stdProb);

        // Bonus symbols with probabilities
        Map<String, Integer> bonusWeights = new HashMap<>();
        bonusWeights.put("5x", 10);
        bonusWeights.put("+500", 5);
        bonusWeights.put("MISS", 85);

        config.probabilities.bonus_symbols = new scratchgame.model.BonusSymbolsProb();
        config.probabilities.bonus_symbols.symbols = bonusWeights;

        // Define standard symbols for RewardCalculator
        config.symbols = new HashMap<>();
        config.symbols.put("A", new SymbolConfig("standard", 5));
        config.symbols.put("B", new SymbolConfig("standard", 3));
        config.symbols.put("C", new SymbolConfig("standard", 2));

        // Define bonus symbols in the config
        config.symbols.put("5x", new SymbolConfig("bonus", 5, "multiply_reward"));
        config.symbols.put("+500", new SymbolConfig("bonus", 0, "extra_bonus", 500));
        config.symbols.put("MISS", new SymbolConfig("bonus", 0, "miss"));

        // Winning combinations
        config.win_combinations = new HashMap<>();
        config.win_combinations.put("same_symbol_3_times", new WinCombination("same_symbols", 3, 1, "same_symbols"));
    }

    @Test
    public void testDefaultBet() {
        String input = "\n"; // Pressing Enter (empty input)
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        GameSession.playRound(scanner, config);
    }

    @Test
    public void testValidBet() {
        String input = "200\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        GameSession.playRound(scanner, config);
    }

    @Test
    public void testInvalidBet() {
        String input = "invalid\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        GameSession.playRound(scanner, config);
    }

    @Test
    public void testMultipleInputs() {
        String input = "300\nn\n"; // Bet 300 and then decline new game
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        GameSession.playRound(scanner, config);
    }
}
