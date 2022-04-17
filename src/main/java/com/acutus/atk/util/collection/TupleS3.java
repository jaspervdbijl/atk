package com.acutus.atk.util.collection;

public class TupleS3<T> extends Tuple3<T,T,T> {

    public TupleS3() {
    }

    public TupleS3(Tuple3<T,T,T> t5) {
        super((T) t5.getFirst(), (T) t5.getSecond(), (T) t5.getThird());
    }
}
