package org.nobloat.bare;

import java.util.ArrayList;
import java.util.List;

public class Array<T> {
    public final int size;
    public List<T> values;

    public Array(int size) {
        this.size = size;
        this.values = new ArrayList<>(size);
        for(int i=0; i < size; i++) {
            values.add(null);
        }
    }

    public void set(int index, T value) {
        this.values.set(index, value);
    }
    public <T> T get(int index) {
        return (T)values.get(index);
    }

    @Override
    public String toString() {
        return "Array{" +
                "size=" + size +
                ", values=" + values +
                '}';
    }
}
