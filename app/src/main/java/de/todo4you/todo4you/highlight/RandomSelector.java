package de.todo4you.todo4you.highlight;

import java.util.List;
import java.util.Random;

import de.todo4you.todo4you.model.Todo;

public class RandomSelector implements HighlightSelector {
    Random rnd = new Random(System.currentTimeMillis());

    @Override
    public Todo select(List<Todo> todos) {
        int next = rnd.nextInt(todos.size());
        return todos.get(next);
    }
}
