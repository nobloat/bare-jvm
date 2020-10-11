package org.nobloat.bare;

import java.util.ArrayList;

public class Array<T> {
    public final int size;
    public ArrayList<T> values;

    public Array(int size) {
        this.size = size;
        this.values = new ArrayList<>(size);
        for(int i=0; i <size; i++) {
            values.add(null);
        }
    }
    public T get(int index) {
        return values.get(index);
    }

    public void set(int index, T value) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        this.values.set(index, value);
    }
}
