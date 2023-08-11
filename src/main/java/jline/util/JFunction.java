package jline.util;

import java.io.Serializable;
import java.util.function.Function;

public interface JFunction<T, U> extends Function<T, U>, Serializable { }
