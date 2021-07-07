package com.example.sharp;


public class LinkedListNode<T extends Object> {
    LinkedList<T> Parent;

    public T Value;
    public LinkedListNode<T> Previous;
    public LinkedListNode<T> Next;
    
    public LinkedList<T> getParent(){
    	return Parent;
    }
    
    /**
     * move to before node
     * @param before
     */
    public void MoveBefore(LinkedListNode<T> node) {
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
    public void MoveAfter(LinkedListNode<T> node) {
    	if(node.Next == this && node == this.Previous) {
    		return;
    	}
        this.Remove();
        node.AddAfter(this);
    }
    public LinkedListNode(LinkedList<T> Parent) {
        this.Parent = Parent;
    }

    public LinkedListNode(LinkedList<T> Parent, T value) {
        this(Parent);
        this.Value = value;
    }

    public void AddAfter(LinkedListNode<T> next) {
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
    }

    public void AddBefore(LinkedListNode<T> prev) {
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
