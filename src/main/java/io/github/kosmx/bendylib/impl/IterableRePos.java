package io.github.kosmx.bendylib.impl;

import java.util.function.Consumer;

@FunctionalInterface
public interface IterableRePos {

    void iteratePositions(Consumer<IPosWithOrigin> consumer);
}
