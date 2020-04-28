package com.example.sharp;

import java.util.Iterator;

import com.example.events.EventDelegate;

/**
 * LinkedList implementation like C# which provides First, Last and iterator
 */
public class LinkedList<T extends Object> implements Iterable<T> {
    LinkedListNode<T> first;
    LinkedListNode<T> last;

    /**
     * First Node
     */
    public Property<LinkedListNode<T>> First = new Property<LinkedListNode<T>>() {
        @Override
        public LinkedListNode<T> get() {
            return first;
        }
    };

    /**
     * Last Node
     */
    public Property<LinkedListNode<T>> Last = new Property<LinkedListNode<T>>() {
        @Override
        public LinkedListNode<T> get() {
            return last;
        }
    };

    /**
     * Count property
     */
    public Property<Integer> Count = new Property<>(0);

    /**
     * IsEmpty property
     */
    public Property<Boolean> IsEmpty = new Property<Boolean>(true) {
        @Override
        public Boolean get() {
            return Count.get() == 0;
        }
    };

    /**
     * EventHandler for notifying when a node was removed
     */
    @SuppressWarnings("rawtypes")
    public EventDelegate NodeRemoved = new EventDelegate();

    /**
     * EventHandler for notifying when a node was added.
     */
    @SuppressWarnings("rawtypes")
    public EventDelegate NodeAdded = new EventDelegate();

    /**
     * Notify list a node was removed.
     *
     * @param node
     */
    void NotifyNodeRemoved(LinkedListNode<T> node) {
        try {
            if (node == this.first) {
                first = first.Next;
            }
            if (node == this.last) {
                last = last.Previous;
            }
            Count.set(Count.get() - 1);
            NodeRemoved.invoke(this, node);
        } catch (Exception e) {
            Tracer.D(e);
        }
    }

    /**
     * Notify list a node was added.
     *
     * @param node the target which was added, not the new node it self
     * @param dir  relation direction to the new node. <0: left, >0: right.
     */
    void NotifyNodeAdded(LinkedListNode<T> node, int dir) {
        NodeAdded.invoke(this, node);
        if (dir < 0) {
            if (node == this.first) {
                first = first.Previous;
            }
        } else if (dir > 0) {
            if (node == this.last) {
                last = last.Next;
            }
        }
        Count.set(Count.get() + 1);
    }

    /**
     * Add to the last of list.
     *
     * @param node node.
     * @return added node.
     */
    public LinkedListNode<T> AddLast(LinkedListNode<T> node) {
        if (this.last == null) {
            this.first = this.last = node;
            this.NotifyNodeAdded(node, 0);
        } else {
            this.last.AddAfter(node);
        }

        return node;
    }

    /**
     * Add value to the last of list.
     *
     * @param value
     * @return new node
     */
    public LinkedListNode<T> AddLast(T value) {
        return AddLast(new LinkedListNode<>(this, value));
    }

    /**
     * Add to the begin of list.
     *
     * @param node node to add.
     * @return new node
     */
    public LinkedListNode<T> AddFirst(LinkedListNode<T> node) {
        if (this.last == null) {
            this.first = this.last = node;
            this.NotifyNodeAdded(node, 0);
        } else {
            this.first.AddBefore(node);
        }

        return node;
    }

    /**
     * Add value to the begin of list.
     *
     * @param value given value
     * @return new node with given value
     */
    public LinkedListNode<T> AddFirst(T value) {
        return AddFirst(new LinkedListNode<>(this, value));
    }

    public LinkedListNode<T> RemoveFirst() {
        LinkedListNode<T> ret = this.first;
        if (ret == null) {
            return ret;
        }

        ret.Remove();
        ret.Previous = null;
        ret.Next = null;

        return ret;
    }

    public LinkedListNode<T> RemoveLast() {
        LinkedListNode<T> ret = this.last;
        if (ret == null) {
            return ret;
        }

        ret.Remove();
        ret.Previous = null;
        ret.Next = null;

        return ret;
    }

    public void Clear() {
        this.first = null;
        this.last = null;
        this.Count.set(0);
    }

    /**
     * Get iterator to traverse this list.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            LinkedListNode<T> node = First.get();

            @Override
            public boolean hasNext() {
                return node != null;
            }

            @Override
            public T next() {
                T ret = node.Value;
                node = node.Next;
                return ret;
            }
        };
    }
}