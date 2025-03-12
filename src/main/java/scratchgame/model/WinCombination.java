package scratchgame.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WinCombination {
    public WinCombination(String when, Integer count, double reward_multiplier, String group) {
        this.when = when;
        this.count = count;
        this.reward_multiplier = reward_multiplier;
        this.group = group;
    }

    public WinCombination(String when, Integer count, double reward_multiplier, String group, List<List<String>> covered_areas) {
        this.when = when;
        this.count = count;
        this.reward_multiplier = reward_multiplier;
        this.group = group;
        this.covered_areas = covered_areas;
    }

    public WinCombination() {
    }

    public double reward_multiplier;
    public String when;
    public Integer count;
    public String group;
    public List<List<String>> covered_areas;
}