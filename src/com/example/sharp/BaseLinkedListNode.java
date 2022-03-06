package com.example.sharp;

public class BaseLinkedListNode<T extends Object> {
    BaseLinkedList<T> Parent;

    public T Value;
    public BaseLinkedListNode<T> Previous;
    public BaseLinkedListNode<T> Next;
    
    public BaseLinkedList<T> getParent(){
        return Parent;
    }
    
    /**
     * move to before node
     * @param before
     */
    public void MoveBefore(BaseLinkedListNode<T> node) {
        if(node.Previous == this && node == this.Next) {
            return;
        }
        this.Remove();
        node.AddBefore(this);
    }
    /**
     * move to before node
     * @param before
     */
    public void MoveAfter(BaseLinkedListNode<T> node) {
        if(node.Next == this && node == this.Previous) {
            return;
        }
        this.Remove();
        node.AddAfter(this);
    }
    public BaseLinkedListNode(BaseLinkedList<T> Parent) {
        this.Parent = Parent;
    }

    public BaseLinkedListNode(BaseLinkedList<T> Parent, T value) {
        this(Parent);
        this.Value = value;
    }
    public BaseLinkedListNode<T> AddAfter(T next) {
        return AddAfter(new BaseLinkedListNode<>(this.Parent, next));
    }
    public BaseLinkedListNode<T> AddAfter(BaseLinkedListNode<T> next) {
        if (this.Next != null) {
            this.Next.Previous = next;
        }
        next.Next = this.Next;
        this.Next = next;
        next.Previous = this;
        next.Parent=this.Parent;
        if(this.Parent != null) {
           this.Parent.NotifyNodeAdded(this, 1);
        }
        return next;
    }

    public BaseLinkedListNode<T> AddBefore(BaseLinkedListNode<T> prev) {
        if (this.Previous != null) {
            this.Previous.Next = prev;
        }
        prev.Previous = this.Previous;
        this.Previous = prev;
        prev.Next = this;
        prev.Parent=this.Parent;
        if(this.Parent != null) {
           this.Parent.NotifyNodeAdded(this, -1);
        }
        return prev;
    }
    public BaseLinkedListNode<T> AddBefore(T prev) {
        return AddBefore(new BaseLinkedListNode<>(Parent,prev)); 
    }
    public void Remove() {
        
        if (this.Previous != null) {
            this.Previous.Next = this.Next;
        }
        if (this.Next != null) {
            this.Next.Previous = this.Previous;
        }
        if(this.Parent != null) {
           this.Parent.NotifyNodeRemoved(this);
        }
        // change link afterward for event handler to change List.First and List.Last
        this.Previous = null;
        this.Next = null;
        this.Parent = null;
    }
}
