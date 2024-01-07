package net.optifine.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class IteratorCache {
    private static final Deque<IteratorCache.IteratorReusable<Object>> dequeIterators = new ArrayDeque<>();

    public static <T> Iterator<T> getReadOnly(List<T> list) {
        synchronized (dequeIterators) {
            IteratorReusable<T> iteratorreusable = (IteratorReusable<T>) dequeIterators.pollFirst();

            if (iteratorreusable == null) {
                iteratorreusable = new IteratorCache.IteratorReadOnly<>();
            }

            iteratorreusable.setList(list);
            return iteratorreusable;
        }
    }

    private static <T> void finished(IteratorCache.IteratorReusable<T> iterator) {
        synchronized (dequeIterators) {
            if (dequeIterators.size() <= 1000) {
                iterator.setList(null);
                dequeIterators.addLast((IteratorReusable<Object>) iterator);
            }
        }
    }

    static {
        for (int i = 0; i < 1000; ++i) {
            IteratorCache.IteratorReadOnly iteratorcache$iteratorreadonly = new IteratorCache.IteratorReadOnly();
            dequeIterators.add(iteratorcache$iteratorreadonly);
        }
    }

    public static class IteratorReadOnly<T> implements IteratorCache.IteratorReusable<T> {
        private List<T> list;
        private int index;
        private boolean hasNext;

        public void setList(List<T> list) {
            if (this.hasNext) {
                throw new RuntimeException("Iterator still used, oldList: " + this.list + ", newList: " + list);
            } else {
                this.list = list;
                this.index = 0;
                this.hasNext = list != null && this.index < list.size();
            }
        }

        public T next() {
            if (!this.hasNext) {
                return null;
            } else {
                T object = this.list.get(this.index);
                ++this.index;
                this.hasNext = this.index < this.list.size();
                return object;
            }
        }

        public boolean hasNext() {
            if (!this.hasNext) {
                IteratorCache.finished(this);
                return false;
            } else {
                return this.hasNext;
            }
        }

    }

    public interface IteratorReusable<E> extends Iterator<E> {
        void setList(List<E> var1);
    }
}
