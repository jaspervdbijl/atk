package com.acutus.atk.util.collection;

public class TupleS4<T> extends Tuple4<T,T,T,T> {

    public TupleS4() {
    }

    public TupleS4(Tuple4 t5) {
        super((T) t5.getFirst(), (T) t5.getSecond(), (T) t5.getThird(), (T) t5.getFourth());
    }
}
