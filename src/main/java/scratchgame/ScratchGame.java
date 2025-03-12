package scratchgame;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        // ПАРСИНГ АРГУМЕНТОВ
        String configPath = null;
        int betAmount = 100;
        for (int i = 0; i < args.length; i++) {
            if ("--config".equals(args[i]) && i + 1 < args.length) {
                configPath = args[i + 1];
            } else if ("--betting-amount".equals(args[i]) && i + 1 < args.length) {
                betAmount = Integer.parseInt(args[i + 1]);
            }
        }
        if (configPath == null) {
            System.err.println("Usage: java -jar <jarfile> --config config.json --betting-amount 100");
            System.exit(1);
        }

        // ЧТЕНИЕ КОНФИГУРАЦИИ
        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(new File(configPath), Config.class);

        // Зададим размер матрицы (если не указано, возьмём 3x3 по умолчанию)
        int rows = (config.rows == null) ? 3 : config.rows;
        int cols = (config.columns == null) ? 3 : config.columns;

        // ГЕНЕРАЦИЯ МАТРИЦЫ
        String[][] matrix = generateMatrix(config, rows, cols);

        // ВЫЧИСЛЕНИЕ ВЫИГРЫША
        CalculationResult calcResult = calculateReward(matrix, betAmount, config);

        // ФОРМИРУЕМ ВЫХОДНОЙ JSON
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("matrix", matrix);
        output.put("reward", calcResult.finalReward);
        output.put("applied_winning_combinations", calcResult.appliedCombinations);
        output.put("applied_bonus_symbol", calcResult.appliedBonusSymbol);

        String jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output);
        System.out.println(jsonOutput);
    }

    /**
     * Генерация матрицы 2D (rows x cols).
     * Выбираем либо стандартный символ, либо бонус — согласно вероятностям.
     */
    private static String[][] generateMatrix(Config config, int rows, int cols) {
        String[][] matrix = new String[rows][cols];
        Random rand = new Random();

        // Если в config.probabilities.standard_symbols не хватает записей,
        // будем использовать первую запись как fallback
        List<StandardSymbolsProb> stdProb = config.probabilities.standard_symbols;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // 1) Решаем, будет ли бонус или стандарт
                //    Допустим, 15% шанс на бонус, 85% на стандарт (пример)
                boolean isBonus = (rand.nextInt(100) < 15);

                // 2) Если стандарт
                if (!isBonus) {
                    // Ищем подходящую настройку вероятностей (row, column)
                    // Если не найдена – используем stdProb.get(0) как запасной вариант
                    StandardSymbolsProb probForCell = findProbForCell(stdProb, r, c);
                    matrix[r][c] = pickSymbolByWeights(probForCell.symbols);
                } else {
                    // 3) Если бонус
                    //    Берём из config.probabilities.bonus_symbols.symbols
                    matrix[r][c] = pickSymbolByWeights(config.probabilities.bonus_symbols.symbols);
                }
            }
        }
        return matrix;
    }

    /**
     * Находим настройку вероятностей для ячейки (row, col) или возвращаем null.
     */
    private static StandardSymbolsProb findProbForCell(List<StandardSymbolsProb> list, int row, int col) {
        for (StandardSymbolsProb s : list) {
            if (s.row == row && s.column == col) {
                return s;
            }
        }
        // Если не найдено - вернём первую/любую запись (fallback)
        return list.get(0);
    }

    /**
     * Функция выбирает символ из мапы { "A": 1, "B": 2, "C": 3, ... }
     * согласно весам. Суммируем все веса и случайным образом
     * выбираем символ, попадая в соответствующий диапазон.
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
        // Теоретически не должны сюда попасть
        return "MISS";
    }

    /**
     * Основная логика вычисления выигрыша:
     * 1) Считаем, какие выигрышные комбинации сработали для стандартных символов.
     * 2) Суммируем итоговый выигрыш.
     * 3) Применяем бонус (если есть).
     */
    private static CalculationResult calculateReward(String[][] matrix, int bet, Config config) {
        double totalReward = 0.0;
        Map<String, List<String>> appliedCombinations = new LinkedHashMap<>();

        // Сгруппируем все символы, чтобы легко проверить повторы
        Map<String, Integer> symbolCount = new HashMap<>();
        int rows = matrix.length;
        int cols = matrix[0].length;

        // Собираем все стандартные символы и их количество
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String sym = matrix[r][c];
                // Проверим, стандартный ли это символ
                SymbolConfig symCfg = config.symbols.get(sym);
                if (symCfg != null && "standard".equals(symCfg.type)) {
                    symbolCount.put(sym, symbolCount.getOrDefault(sym, 0) + 1);
                }
            }
        }

        // Для каждого стандартного символа будем вычислять множители
        // по группам winning_combinations
        // Итого reward для одного символа: (bet * symbolMultiplier) * productOfAllAppliedGroups
        // Затем суммируем по всем символам.
        for (Map.Entry<String, Integer> entry : symbolCount.entrySet()) {
            String symbol = entry.getKey();
            int count = entry.getValue();

            double symbolBaseMultiplier = config.symbols.get(symbol).reward_multiplier;
            double finalMultiplierForSymbol = 1.0;
            List<String> combosForSymbol = new ArrayList<>();

            // 1) Проверяем группу "same_symbols"
            //    Ищем максимально возможную (3,4,5,...9)
            Optional<Map.Entry<String, WinCombination>> bestSameSymbolCombo =
                    config.win_combinations.entrySet().stream()
                            .filter(e -> "same_symbols".equals(e.getValue().when))
                            .filter(e -> e.getValue().count != null && count >= e.getValue().count)
                            .max(Comparator.comparingInt(e -> e.getValue().count));
            if (bestSameSymbolCombo.isPresent()) {
                // Применяем её
                WinCombination wc = bestSameSymbolCombo.get().getValue();
                finalMultiplierForSymbol *= wc.reward_multiplier;
                combosForSymbol.add(bestSameSymbolCombo.get().getKey()); // имя комбинации
            }

            // 2) Проверяем "linear_symbols" (горизонталь, вертикаль, диагонали)
            //    Но по условию «max 1 winning combination should be applied for each group».
            //    Группа хранится в поле `group`. Нужно понять, активировалась ли уже какая-то
            //    комбинация из той же группы. Для упрощения — просто поочерёдно проверим каждую,
            //    и если находим совпадение, применяем множитель и не даём применяться другой
            //    из той же группы.
            Set<String> usedGroups = new HashSet<>();
            if (bestSameSymbolCombo.isPresent()) {
                // Мы уже заняли группу "same_symbols", но это не мешает linear-типам, т.к. у них другие group
                usedGroups.add(bestSameSymbolCombo.get().getValue().group);
            }

            for (Map.Entry<String, WinCombination> wcEntry : config.win_combinations.entrySet()) {
                WinCombination wc = wcEntry.getValue();
                if (!"linear_symbols".equals(wc.when)) {
                    continue;
                }
                // Если группа уже применена для этого символа, пропускаем
                if (usedGroups.contains(wc.group)) {
                    continue;
                }
                // Проверяем, действительно ли символ заполняет все ячейки из covered_areas
                // covered_areas – список списков, каждая "линия" – набор координат
                // Если символ занимает хотя бы одну такую линию полностью, считаем комбинацию выполненной.
                if (checkLinearCombo(matrix, symbol, wc.covered_areas)) {
                    // Применяем
                    finalMultiplierForSymbol *= wc.reward_multiplier;
                    combosForSymbol.add(wcEntry.getKey());
                    usedGroups.add(wc.group);
                }
            }

            // Если для символа вообще не было никаких комбинаций, он даёт 0
            // Если хотя бы одна комбинация применена, считаем выигрыш
            if (combosForSymbol.isEmpty()) {
                continue;
            }

            // Частичный выигрыш для символа
            double partialReward = bet * symbolBaseMultiplier * finalMultiplierForSymbol;
            totalReward += partialReward;
            appliedCombinations.put(symbol, combosForSymbol);
        }

        // Теперь смотрим, есть ли в матрице бонус-символ
        // и если totalReward > 0, то применяем первый найденный
        String appliedBonusSymbol = null;
        if (totalReward > 0) {
            bonusLoop:
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    String sym = matrix[r][c];
                    SymbolConfig symCfg = config.symbols.get(sym);
                    if (symCfg != null && "bonus".equals(symCfg.type)) {
                        appliedBonusSymbol = sym;
                        if ("multiply_reward".equals(symCfg.impact)) {
                            totalReward *= symCfg.reward_multiplier;
                        } else if ("extra_bonus".equals(symCfg.impact)) {
                            totalReward += symCfg.extra;
                        }
                        // MISS - ничего не делаем
                        break bonusLoop;
                    }
                }
            }
        }

        // Округлим до целых (или оставим как double — зависит от ТЗ)
        long finalReward = Math.round(totalReward);

        CalculationResult result = new CalculationResult();
        result.finalReward = finalReward;
        result.appliedCombinations = appliedCombinations;
        result.appliedBonusSymbol = appliedBonusSymbol;
        return result;
    }

    /**
     * Проверяем, заполняет ли данный symbol какую-то "линию" из covered_areas полностью.
     * covered_areas – список линий. Достаточно, чтобы символ занял хотя бы одну из них.
     */
    private static boolean checkLinearCombo(String[][] matrix, String symbol, List<List<String>> coveredAreas) {
        for (List<String> line : coveredAreas) {
            boolean matchLine = true;
            for (String cell : line) {
                // cell формата "row:col"
                String[] parts = cell.split(":");
                int rr = Integer.parseInt(parts[0]);
                int cc = Integer.parseInt(parts[1]);
                if (!matrix[rr][cc].equals(symbol)) {
                    matchLine = false;
                    break;
                }
            }
            if (matchLine) {
                return true; // нашли линию, полностью заполненную символом
            }
        }
        return false;
    }

    // ----- Вспомогательные классы для парсинга конфигурации -----

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Config {
        public Integer columns;
        public Integer rows;
        public Map<String, SymbolConfig> symbols;
        public Probabilities probabilities;
        public Map<String, WinCombination> win_combinations;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SymbolConfig {
        public double reward_multiplier; // для bonus: множитель, для standard: множитель
        public String type;             // "standard" или "bonus"
        public Integer extra;           // только для extra_bonus
        public String impact;           // "multiply_reward", "extra_bonus", "miss"
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Probabilities {
        public List<StandardSymbolsProb> standard_symbols;
        public BonusSymbolsProb bonus_symbols;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class StandardSymbolsProb {
        public int column;
        public int row;
        public Map<String, Integer> symbols;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class BonusSymbolsProb {
        public Map<String, Integer> symbols;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class WinCombination {
        public double reward_multiplier;
        public String when;     // "same_symbols" или "linear_symbols"
        public Integer count;   // для same_symbols
        public String group;    // для ограничения "max 1 combination per group"
        public List<List<String>> covered_areas; // для linear_symbols
    }

    static class CalculationResult {
        public long finalReward;
        public Map<String, List<String>> appliedCombinations;
        public String appliedBonusSymbol;
    }
}