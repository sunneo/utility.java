package com.example.sharp;

public class LinkedListNode<T extends Object> extends BaseLinkedListNode<T> {

    
    public LinkedList<T> getParent(){
    	return (LinkedList<T>)Parent;
    }
    
   
    public LinkedListNode(BaseLinkedList<T> Parent) {
        super(Parent);
        this.Parent = Parent;
    }

    public LinkedListNode(BaseLinkedList<T> Parent, T value) {
        this(Parent);
        this.Value = value;
    }
   
}
