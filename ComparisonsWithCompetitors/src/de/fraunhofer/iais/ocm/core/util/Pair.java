package de.fraunhofer.iais.ocm.core.util;

/**
 * Created by IntelliJ IDEA.
 * Date: 25/11/13
 */
public class Pair <K, V> {

    private final K element0;

    private final V element1;

    public Pair(K element0, V element1) {
        this.element0 = element0;
        this.element1 = element1;
    }

    public K getElement0() {
        return element0;
    }

    public V getElement1() {
        return element1;
    }

}
