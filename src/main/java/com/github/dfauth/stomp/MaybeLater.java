package com.github.dfauth.stomp;

import java.util.Optional;
import java.util.function.Consumer;

public class MaybeLater<T> {

    private Optional<T> t = Optional.empty();
    private Optional<Consumer<T>> consumer = Optional.empty();

    public MaybeLater<T> accept(Consumer<T> f) {
        consumer = Optional.of(f);
        return  _doit();
    }

    public MaybeLater<T> accept(T _t) {
        t = Optional.of(_t);
        return  _doit();
    }

    private MaybeLater<T> _doit() {
        consumer.ifPresent(_c -> t.ifPresent(_c));
        return this;
    }

}
