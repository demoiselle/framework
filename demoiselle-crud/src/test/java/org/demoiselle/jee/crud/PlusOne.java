package org.demoiselle.jee.crud;

import java.util.function.Function;

public class PlusOne implements Function<Long, Long> {
    @Override
    public Long apply(Long aLong) {
        return aLong+1;
    }
}
