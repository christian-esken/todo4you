package de.todo4you.todo4you.highlight;

import java.util.Arrays;
import java.util.List;

import de.todo4you.todo4you.model.Idea;

public class ShortCircuitChainedSelector implements HighlightSelector {
    final HighlightSelector[] selectors;

    public ShortCircuitChainedSelector(HighlightSelector... selectors) {
        this.selectors = Arrays.copyOf(selectors, selectors.length);
    }


    @Override
    public Idea select(List<Idea> ideas) {
        if (ideas.isEmpty()) {
            return null;
        }

        for (HighlightSelector selector : selectors) {
            Idea selected = selector.select(ideas);
            if (selected != null) {
                return selected; // short-circuit => exit on first match
            }
        }

        return ideas.get(0);
    }
}
