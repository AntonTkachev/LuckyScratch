package scratchgame.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StandardSymbolsProb {
    public StandardSymbolsProb(int row, int column, Map<String, Integer> symbols) {
        this.row = row;
        this.column = column;
        this.symbols = symbols;
    }

    public StandardSymbolsProb() {
    }

    public int column;
    public int row;
    public Map<String, Integer> symbols;
}