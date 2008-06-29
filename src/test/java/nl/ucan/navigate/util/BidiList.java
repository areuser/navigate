package nl.ucan.navigate.util;

import org.apache.commons.collections.CollectionUtils;

import java.util.*;
/*
 * Copyright 2007-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * author : Arnold Reuser
 * since  : 0.1
  */

public class BidiList<T> extends ArrayList<T> {
    private PreModification<T> ass;
    public BidiList(List<T> list, PreModification<T> preModification) {
        this.ass = preModification;
    }

    public Iterator<T> iterator() {
        return new BidiListIterator(this.listIterator(),this.ass);
    }

    public boolean add(T o) {
        o = this.ass.add(o);
        return super.add(o);
    }

    public boolean remove(Object o) {
        this.ass.remove((T)o);
        return super.remove(o);
    }

    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for(Object e:c) {
            e = this.ass.add((T)e);
            changed |= this.add((T)e);
        }
        return changed;
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        boolean changed = false;
        Object[] a = super.toArray();
        this.clear();
        for(int i=0; i < a.length ; i++) {
            if ( i == index) {
                for(T e:c) {
                    changed |= this.add(e);
                }
            }
            this.add((T)a[i]);
        }
        return changed;
    }

    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for(Object e:c) {
            this.ass.remove((T)e);
            changed |= this.remove(e);
        }
        return changed;
    }

    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        Collection disjunct = CollectionUtils.disjunction(this,c);
        if( disjunct == null  || disjunct.size() == 0  ) return changed;
        else  {
            changed = true;
            Iterator it = disjunct.iterator();
            while(it.hasNext()) {
                T o = (T)it.next();
                this.ass.remove((T)o);
                it.remove();
            }
            return changed;
        }
    }

    public void clear() {
        Iterator it = super.iterator();
        while(it.hasNext()) {
            T o = (T)it.next();
            this.ass.remove((T)o);
            it.remove();
        }       
    }


    public T set(int index, T element) {
        element = this.ass.add((T)element);
        return super.set(index,element);
    }

    public void add(int index, T element) {
        element = this.ass.add((T)element);
        super.add(index,element);
    }

    public T remove(int index) {
        T element = this.get(index);
        this.ass.remove((T)element);
        return super.remove(index);
    }

    public ListIterator<T> listIterator() {
        return new BidiListIterator<T>(super.listIterator(),this.ass);
    }

    public ListIterator listIterator(int index) {
            return new BidiListIterator(super.listIterator(index),this.ass);
    }

    public List subList(int fromIndex, int toIndex) {
        return new BidiList(super.subList(fromIndex,toIndex),this.ass);
    }

    protected class BidiListIterator<T> implements ListIterator<T> {
        private PreModification<T> ass;
        private ListIterator<T> iterator;
        private T last;
        public BidiListIterator(ListIterator<T> iterator, PreModification<T> ass) {
            this.iterator = iterator;
            this.ass = ass;
        }
        public boolean hasNext() {
            return iterator.hasNext();
        }

        public T next() {
            this.last = iterator.next();
            return last;
        }

        public boolean hasPrevious() {
             return iterator.hasPrevious();
        }

        public T previous() {
            this.last = iterator.previous();
            return last;

        }

        public int nextIndex() {
            return iterator.nextIndex();
        }

        public int previousIndex() {
            return iterator.previousIndex();
        }

        public void remove() {
           this.ass.remove(this.last);
           iterator.remove();
        }

        public void set(T o) {
            o = this.ass.add(o);
            iterator.set(o);
        }

        public void add(T o) {
            o = this.ass.add(o);
            iterator.add(o);
        }
    }    
}
