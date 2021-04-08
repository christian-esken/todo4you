package de.todo4you.todo4you.highlight;

import java.util.List;
import java.util.Random;

import de.todo4you.todo4you.model.Idea;

public class RandomSelector implements HighlightSelector {
    Random rnd = new Random(System.currentTimeMillis());

    @Override
    public Idea select(List<Idea> ideas) {
        int next = rnd.nextInt(ideas.size());
        return ideas.get(next);
    }
}
