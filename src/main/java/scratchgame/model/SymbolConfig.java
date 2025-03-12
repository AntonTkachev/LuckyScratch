package scratchgame.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SymbolConfig {

    public SymbolConfig(String type, double reward_multiplier) {
        this.type = type;
        this.reward_multiplier = reward_multiplier;
    }

    public SymbolConfig(String type, double reward_multiplier, String impact) {
        this.type = type;
        this.reward_multiplier = reward_multiplier;
        this.impact = impact;
    }

    public SymbolConfig(String type, double reward_multiplier, String impact, int extra) {
        this.type = type;
        this.reward_multiplier = reward_multiplier;
        this.impact = impact;
        this.extra = extra;
    }

    public SymbolConfig() {
    }

    public double reward_multiplier;
    public String type;
    public Integer extra;
    public String impact;
}