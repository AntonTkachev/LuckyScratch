package scratchgame.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Probabilities {
    public List<StandardSymbolsProb> standard_symbols;
    public BonusSymbolsProb bonus_symbols;
}