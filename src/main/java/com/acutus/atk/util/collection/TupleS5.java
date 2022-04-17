package com.acutus.atk.util.collection;

public class TupleS5<T> extends Tuple5<T,T,T,T,T> {

    public TupleS5() {
    }

    public TupleS5(Tuple5 t5) {
        super((T) t5.getFirst(), (T) t5.getSecond(), (T) t5.getThird(), (T) t5.getFourth(), (T) t5.getFifth());
    }
}
