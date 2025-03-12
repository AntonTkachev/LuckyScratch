package scratchgame.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    public Integer columns;
    public Integer rows;
    public Map<String, SymbolConfig> symbols;
    public Probabilities probabilities;
    public Map<String, WinCombination> win_combinations;
}