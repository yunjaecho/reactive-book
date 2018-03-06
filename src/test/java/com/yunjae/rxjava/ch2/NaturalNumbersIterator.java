package com.yunjae.rxjava.ch2;

import java.math.BigInteger;
import java.util.Iterator;

public class NaturalNumbersIterator implements Iterator<BigInteger> {
    private BigInteger current = BigInteger.ZERO;

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public BigInteger next() {
        current = current.add(BigInteger.ONE);
        return current;
    }
}
