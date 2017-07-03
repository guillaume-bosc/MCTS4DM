package de.fraunhofer.iais.ocm.core.util;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * User: paveltokmakov
 * Date: 5/12/13
 */
public class LimitedPriorityQueue<T> extends PriorityQueue<T> {

    private int sizeLimit;

    public LimitedPriorityQueue(int sizeLimit) {
        super();
        this.sizeLimit = sizeLimit;
    }

    public LimitedPriorityQueue(int initialCapacity, Comparator<? super T> comparator, int sizeLimit) {
        super(initialCapacity, comparator);
        this.sizeLimit = sizeLimit;
    }

    public LimitedPriorityQueue(PriorityQueue<? extends T> ts, int sizeLimit) {
        super(ts);
        this.sizeLimit = sizeLimit;
    }

    @Override
    public boolean add(T t) {
        boolean result = super.add(t);

        if(size() == sizeLimit + 1) {
            poll();
        }

        return result;
    }

}
