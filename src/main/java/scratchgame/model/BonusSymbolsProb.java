package scratchgame.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BonusSymbolsProb {
    public Map<String, Integer> symbols;
}