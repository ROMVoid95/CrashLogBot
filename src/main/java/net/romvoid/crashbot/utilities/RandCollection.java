package net.romvoid.crashbot.utilities;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandCollection<E> {
    private final NavigableMap<Double, E> map = new TreeMap<>();
    private final Random random;
    private double total = 0;

    public RandCollection() {
        this(new Random());
    }

    public RandCollection(Random random) {
        this.random = random;
    }

    public RandCollection<E> add(double weight, E result) {
        if (weight <= 0) {
            return this;
        }

        total += weight;
        map.put(total, result);
        return this;
    }

    public E next() {
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }
}
