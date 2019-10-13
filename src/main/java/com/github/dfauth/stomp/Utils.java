package com.github.dfauth.stomp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;


public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static <T> BinaryOperator<T> getLast() {
        return (first, second) -> second;
    }

    public static <T> Stream<T> optionalStream(Optional<T> o) {
        return o.map(Stream::of).orElseGet(Stream::empty);
    }

    public static <T> Optional<T> doit(Optional<T> o1, Optional<T> o2, BinaryOperator<T> f) {
        return o1.flatMap(o -> o2.map(_o -> f.apply(o, _o)));
    }
}
