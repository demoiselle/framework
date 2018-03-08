package org.demoiselle.jee.crud;

import java.util.function.Function;

public class IdentityFunction implements Function {
    @Override
    public Object apply(Object o) {
        return o;
    }
}
