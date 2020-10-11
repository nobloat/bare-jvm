package org.nobloat.bare;

public class UnionType {
    public final Object value;
    public final int typeId;


    public UnionType(int id, Object object) {
        this.typeId = id;
        this.value = object;
    }
}
