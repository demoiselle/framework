/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.hashcash.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class is meant to simulate a list of infinite (and optionally
 * bi-infinite) length. All positive indices are valid but will default to
 * <code>null</code>. This list can also be bi-infinite, in which case all
 * negative indices are also valid. This class is also useful for sparse lists
 * as it is backed by a {@link java.util.SortedMap} Copyright 2006 Gregory Rubin
 * <a href="mailto:grrubin@gmail.com">grrubin@gmail.com</a><br>
 * Permission is given to use, modify, and or distribute this code so long as
 * this message remains attached<br>
 *
 * @author grrubin@gmail.com
 * @version 1.0
 */
public class InfiniteList<E> implements List<E> {

    private SortedMap<Integer, E> myMap;
    private boolean allowNegative;

    /**
     * Constructs an empty list with <code>allowNegative</code> set to false.
     */
    public InfiniteList() {
        myMap = new TreeMap<Integer, E>();
        allowNegative = false;
    }

    /**
     * Constructs an empty list.
     */
    public InfiniteList(boolean allowNegative) {
        myMap = new TreeMap<Integer, E>();
        this.allowNegative = allowNegative;
    }

    /**
     * Copy constructor
     */
    public InfiniteList(InfiniteList<E> obj) {
        myMap = new TreeMap<Integer, E>(obj.myMap);

        this.allowNegative = obj.allowNegative;
    }

    /**
     * Does this specific instance allow negative indices
     */
    public boolean allowsNegative() {
        return allowNegative;
    }

    public int size() {
        if (myMap.isEmpty()) {
            return 0;
        }
        return (myMap.lastKey().intValue() - myMap.firstKey().intValue() + 1);
    }

    /**
     * Maximum assigned index
     */
    public int max() {
        if (myMap.isEmpty() || null == myMap.lastKey()) {
            return 0;
        } else {
            return (myMap.lastKey().intValue());
        }
    }

    /**
     * Minimum assigned index
     */
    public int min() {
        if (myMap.isEmpty() || null == myMap.firstKey()) {
            return 0;
        } else {
            return (myMap.firstKey().intValue());
        }
    }

    /*
   * How many indices have non-null values
     */
    public int load() {
        return myMap.size();
    }

    public boolean add(E element) {
        if (null == element) {
            throw new NullPointerException("Adding a null element to the end of the list is meaningless.");
        }

        if (isEmpty()) {
            set(0, element);
        } else {
            add(max() + 1, element);
        }

        return true;
    }

    public void add(int index, E element) {
        if (null == element && index > max()) {
            throw new NullPointerException("Adding a null element to the end of the list is meaningless.");
        }

        if (index <= max()) {
            Integer[] indices = myMap.tailMap(Integer.valueOf(index)).keySet().toArray(new Integer[0]);
            Arrays.sort(indices);
            for (int x = indices.length - 1; x >= 0; x--) {
                set(indices[x] + 1, get(indices[x]));
                set(indices[x], null);
            }
        }
        set(index, element);
    }

    /*
   * Add all elements in the collection to this list.
   * This will handle nulls as expected in that if they come before the last element,
   * they will be preserved.
     */
    public boolean addAll(Collection<? extends E> c) {
        if (!isEmpty()) {
            return addAll(max() + 1, c);
        } else {
            return addAll(0, c);
        }
    }

    /*
   * Add all elements in the collection to this list starting at <code>index</code>.
   * This will handle nulls as expected in that if they come before the last element,
   * they will be preserved.
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        if (c.isEmpty()) {
            return false;
        }

        if (index <= max() && !isEmpty()) {
            Integer[] indices = myMap.tailMap(Integer.valueOf(index)).keySet().toArray(new Integer[0]);
            Arrays.sort(indices);
            for (int x = indices.length - 1; x >= 0; x--) {
                set(indices[x] + c.size(), get(indices[x]));
                set(indices[x], null);
            }
        }

        for (E obj : c) {
            set(index, obj);
            index++;
        }

        return true;
    }

    public void clear() {
        myMap.clear();
    }

    public boolean contains(Object o) {
        if (null == o) {
            return true;
        }

        return myMap.containsValue(o);
    }

    public boolean containsAll(Collection<?> o) {
        for (Object obj : o) {
            if (!contains(obj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * All indices with non-null values in ascending order
     */
    public Set<Integer> indexSet() {
        return myMap.keySet();
    }

    /**
     * Acts as expected if other object is an InfiniteList. Else returns true if
     * this list has no negative elements and its positive elements are equal to
     * the list passed in. NOTE: If the other object is not an InfiniteList, we
     * cannot guarantee that
     * <code>infiniteList.equals(o) == o.equals(infiniteList)</code> An example
     * of where this will break is any list that contains nulls on either end.
     */
    public boolean equals(Object o) {
        if (null == o) {
            return false;
        } else if (o instanceof InfiniteList) {
            return ((InfiniteList) o).myMap.equals(myMap);
        } else if (o instanceof List) {
            List tempList = (List) o;
            if (isEmpty() && tempList.isEmpty()) {
                return true;
            } else if (min() >= 0 && max() < tempList.size()) {
                for (int x = 0; x < tempList.size(); x++) {
                    if ((get(x) != null && !get(x).equals(tempList.get(x)))
                            || (tempList.get(x) != null && !tempList.get(x).equals(get(x)))) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public E get(int index) {
        if (!allowNegative && index < 0) {
            throw new IndexOutOfBoundsException();
        }
        return myMap.get(Integer.valueOf(index));
    }

    public int hashCode() { // TODO: Improve
        int hashCode = 1;
        ListIterator i = listIterator(0);
        while (i.hasNext()) {
            Object obj = i.next();
            hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
        }
        i = listIterator(0);
        while (i.hasPrevious()) {
            Object obj = i.previous();
            hashCode = 37 * hashCode + (obj == null ? 0 : obj.hashCode());
        }

        return hashCode;
    }

    /**
     * WARNING! DO NOT USE THIS TO DETERMINE IF THE LIST CONTAINS AN ELEMENT AS
     * -1 IS A VALID RESPONSE! USE {@link #contains(Object o)} If <code>o</code>
     * is <code>null</code> then returns {@link #min()} - 1
     */
    public int indexOf(Object o) {
        if (null == o) {
            return min() - 1;
        } else if (!myMap.containsValue(o)) {
            return -1;
        } else {
            for (Map.Entry<Integer, E> entry : myMap.entrySet()) {
                if (entry.getValue().equals(o)) {
                    return entry.getKey().intValue();
                }
            }
            return -1; // Unreachable line
        }
    }

    public boolean isEmpty() {
        return myMap.isEmpty();
    }

    /**
     * WARNING! DO NOT USE THIS TO DETERMINE IF THE LIST CONTAINS AN ELEMENT AS
     * -1 IS A VALID RESPONSE! USE {@link #contains(Object o)} WARNING! THIS IS
     * A SLOW IMPLEMENTATION If <code>o</code> is <code>null</code> then returns
     * {@link #max()} + 1
     */
    public int lastIndexOf(Object o) {
        if (null == o) {
            return max() + 1;
        } else if (!myMap.containsValue(o)) {
            return -1;
        } else {
            int result = -1;
            for (Map.Entry<Integer, E> entry : myMap.entrySet()) {
                if (entry.getValue().equals(o)) {
                    result = entry.getKey().intValue();
                }
            }
            return result;
        }
    }

    public Iterator<E> iterator() {
        return listIterator();
    }

    public ListIterator<E> listIterator() {
        return new InfiniteListIterator<E>(this);
    }

    public ListIterator<E> listIterator(int index) {
        return new InfiniteListIterator<E>(this, index);
    }

    public E remove(int index) {
        E old = get(index);
        set(index, null);
        Set<Integer> indices = new TreeSet<Integer>(myMap.tailMap(Integer.valueOf(index)).keySet());
        for (Integer x : indices) {
            set(x.intValue() - 1, get(x.intValue()));
            set(x.intValue(), null);
        }
        return old;
    }

    public boolean remove(Object o) {
        if (null == o) {
            return true; // We always contain the null element
        }
        if (contains(o)) {
            int index = indexOf(o);
            remove(index);
            return true;
        } else {
            return false;
        }
    }

    public E set(int index, E element) {
        if (!allowNegative && index < 0) {
            throw new IndexOutOfBoundsException();
        }

        if (null != element) {
            return myMap.put(Integer.valueOf(index), element);
        } else {
            return myMap.remove(Integer.valueOf(index));
        }
    }

    public InfiniteList<E> subList(int fromIndex, int toIndex) {
        InfiniteList<E> result = new InfiniteList<E>();
        result.myMap = myMap.subMap(Integer.valueOf(fromIndex), Integer.valueOf(toIndex));
        result.allowNegative = allowNegative;

        return result;
    }

    public boolean retainAll(Collection<?> c) {
        return myMap.values().retainAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return myMap.values().removeAll(c);
    }

    /**
     * NOTE: While order is guaranteed, indices are not guarenteed to be
     * identical unless all are non-negative
     */
    public Object[] toArray() {
        return toArray(new Object[0]);
    }

    /**
     * NOTE: While order is guaranteed, indices are not guarenteed to be
     * identical unless all are non-negative
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        Arrays.fill(a, null);

        int offset = (min() < 0
                ? -1 * min()
                : 0);

        // We mock this up by building a normal list and sending that back
        List<T> tempList = new ArrayList<T>(max() + offset);
        if (!isEmpty()) {
            for (int x = 0; x <= max() + offset; x++) {
                tempList.add((T) get(x - offset));
            }
        }

        return (T[]) tempList.toArray(a);
    }

    private static class InfiniteListIterator<E> implements ListIterator<E> {

        private boolean tainted = true;
        private InfiniteList<E> myList;
        private int nextIndex;
        private int previousIndex;
        private int lastIndex;

        public InfiniteListIterator(InfiniteList<E> list) {
            myList = list;
            nextIndex = list.min();
            previousIndex = nextIndex - 1;
        }

        public InfiniteListIterator(InfiniteList<E> list, int index) {
            myList = list;
            nextIndex = index;
            previousIndex = nextIndex - 1;
        }

        public boolean hasNext() {
            if (myList.isEmpty()) {
                return false;
            }
            return myList.max() >= nextIndex;
        }

        public boolean hasPrevious() {
            if (myList.isEmpty()) {
                return false;
            }
            return myList.min() <= previousIndex;
        }

        public E next() {
            tainted = false;
            previousIndex = nextIndex;
            lastIndex = nextIndex;
            return myList.get(nextIndex++);
        }

        public E previous() {
            tainted = false;
            nextIndex = previousIndex;
            lastIndex = previousIndex;
            return myList.get(previousIndex--);
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return previousIndex;
        }

        public void remove() {
            if (tainted) {
                throw new IllegalStateException();
            }
            tainted = true;
            myList.remove(lastIndex);
            nextIndex--;
        }

        public void add(E o) {
            myList.add(nextIndex, o);
            nextIndex++;
            previousIndex = nextIndex - 1;
            tainted = true;
        }

        public void set(E o) {
            if (tainted) {
                throw new IllegalStateException();
            }

            myList.set(lastIndex, o);
        }
    };
}
