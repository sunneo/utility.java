package com.example.sharp;

import com.example.events.WritableValue;

import java.util.Iterator;


/**
 * BaseLinkedList implementation like C# which provides First, Last and iterator
 * with no notifications
 */
@SuppressWarnings("rawtypes")
public class BaseLinkedList <T extends Object> implements Iterable<T> {
    
    protected static long seriesId=0;
    long Id=seriesId++;
    public long getId() {
        return Id;
    }
    BaseLinkedListNode<T> first;
    BaseLinkedListNode<T> last;

    public void dispose() {
        
        
        this.First = null;
        this.Last = null;
        IsEmpty=null;
        if(Count!=null) {
            Count.dispose();
        }
        Count=null;
        
    }
    
    /**
     * First Node
     */
    public IGetter<BaseLinkedListNode<T>> First = new IGetter<BaseLinkedListNode<T>>() {
        @Override
        public BaseLinkedListNode<T> get() {
            return first;
        }
    };
    public boolean Remove(T val) {
        for(BaseLinkedListNode<T> node=first; node != null; node=node.Next) {
            if(node.Value==val) {
                node.Remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Last Node
     */
    public IGetter<BaseLinkedListNode<T>> Last = new IGetter<BaseLinkedListNode<T>>() {
        @Override
        public BaseLinkedListNode<T> get() {
            return last;
        }
    };

    /**
     * Count property
     */
    public WritableValue<Integer> Count = new WritableValue<>(0);

    /**
     * IsEmpty property
     */
    public IGetter<Boolean> IsEmpty = new IGetter<Boolean>(){
        @Override
        public Boolean get() {
            return Count.get() == 0;
        }
    };

   

    /**
     * Notify list a node was removed.
     * @param node
     */
    @SuppressWarnings("unchecked")
    void NotifyNodeRemoved(BaseLinkedListNode<T> node) {
        try {
            if (node == this.first) {
                first = first.Next;
            }
            if (node == this.last) {
                last = last.Previous;
            }
            Count.set(Count.get() - 1);
        } catch (Exception e) {
            Tracer.D(e);
        }
    }

    /**
     * Notify list a node was added.
     * @param node the target which was added, not the new node it self
     * @param dir  relation direction to the new node. <0: left, >0: right.
     */
    @SuppressWarnings("unchecked")
    void NotifyNodeAdded(BaseLinkedListNode<T> node, int dir) {
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
     * @param node node.
     * @return added node.
     */
    public BaseLinkedListNode<T> AddLast(BaseLinkedListNode<T> node) {
        if (this.last == null) {
            this.first = this.last = node;
            node.Parent = this;
            this.NotifyNodeAdded(node, 0);
        } else {
            this.last.AddAfter(node);
        }

        return node;
    }

    /**
     * Add value to the last of list.
     * @param value
     * @return new node
     */
    public BaseLinkedListNode<T> AddLast(T value) {
        return AddLast(new BaseLinkedListNode<>(this, value));
    }

    /**
     * Add to the begin of list.
     * @param node node to add.
     * @return new node
     */
    public BaseLinkedListNode<T> AddFirst(BaseLinkedListNode<T> node) {
        if (this.last == null) {
            this.first = this.last = node;
            node.Parent = this;
            this.NotifyNodeAdded(node, 0);
        } else {
            this.first.AddBefore(node);
        }

        return node;
    }

    /**
     * Add value to the begin of list.
     * @param value given value
     * @return new node with given value
     */
    public BaseLinkedListNode<T> AddFirst(T value) {
        return AddFirst(new BaseLinkedListNode<>(this, value));
    }

    public BaseLinkedListNode<T> RemoveFirst() {
        BaseLinkedListNode<T> ret = this.first;
        if (ret == null) {
            return ret;
        }

        ret.Remove();
        return ret;
    }

    public BaseLinkedListNode<T> RemoveLast() {
        BaseLinkedListNode<T> ret = this.last;
        if (ret == null) {
            return ret;
        }

        ret.Remove();
        return ret;
    }

    public void Clear() {
        this.first = null;
        this.last = null;
        this.Count.set(0);
      
    }

    /**
     * Get back-iterable to traverse this list.
     */
    public Iterable<T> backiterable(){
        return new Iterable<T>() {
            public Iterator<T> iterator(){
                return backiterator();
            }
        };
    }
    /**
     * Get back-iterator to traverse this list.
     */
    public Iterator<T> backiterator() {
        return new Iterator<T>() {
            BaseLinkedListNode<T> node = Last.get();

            @Override
            public boolean hasNext() {
                return node != null;
            }

            @Override
            public T next() {
                if(node == null) {
                    return null;
                }
                T ret = node.Value;
                node = node.Previous;
                return ret;
            }
        };
    }
    /**
     * Get iterator to traverse this list.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Dictionary<BaseLinkedListNode<T>,BaseLinkedListNode<T>> visited = new Dictionary<>();
            BaseLinkedListNode<T> node = First.get();

            @Override
            public boolean hasNext() {
                return node != null;
            }

            @Override
            public T next() {
                
                if(node == null) {
                    return null;
                }
                
                T ret = node.Value;
                visited.set(node, node);
                BaseLinkedListNode<T> nextNode = node.Next;
                while( nextNode!=null && visited.containsKey(nextNode)) {
                    nextNode=nextNode.Next;
                }
                node = nextNode;
                
                return ret;
            }
        };
    }

    public void add(T value) {
        this.AddLast(value);
    }
    public int size() {
        return this.Count.get();
    }
    public void clear() {
        this.Clear();
    }

    public boolean remove(T t) {
        for(BaseLinkedListNode<T> node=First.get(); node!=null; node=node.Next) {
            if(node.Value == t) {
                node.Remove();
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return IsEmpty.get();
    }
}