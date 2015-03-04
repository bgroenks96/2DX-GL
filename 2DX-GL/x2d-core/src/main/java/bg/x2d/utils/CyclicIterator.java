package bg.x2d.utils;

import java.util.Iterator;

public class CyclicIterator<E> implements Iterator<E> {

    private final Iterable<E> iterable;

    private Iterator<E> iteration;

    public CyclicIterator(Iterable<E> iterable) {
        this.iterable = iterable;

        iteration = iterable.iterator();
    }

    /**
     * Returns true if this CyclicIterator has reached the end of the current iteration, false otherwise.
     * Further calls to {@link #next()} will reset the current Iterator to a new instance.
     */
    @Override
    public boolean hasNext() {
        return iteration.hasNext();
    }

    /**
     * Advances the Iterable provided to this CyclicIterator. If the end of the iteration is reached, this method will
     * create a new Iterator instance and "cycle" through to the beginning of the iterable sequence.
     */
    @Override
    public E next() {
        if (!iteration.hasNext()) iteration = iterable.iterator();
        return iteration.next();
    }

    @Override
    public void remove() {
        iteration.remove();
    }
}
