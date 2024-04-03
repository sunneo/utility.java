package com.example.sharp;

import com.example.events.EventDelegate;
import com.example.events.INotification;
import com.example.events.INotificationEventArgs;


/**
 * LinkedList implementation like C# which provides First, Last and iterator
 */
@SuppressWarnings("rawtypes")
public class LinkedList<T extends Object> extends BaseLinkedList<T>{
	public static interface NodeChangedEventHandler<T> extends INotification<INotificationEventArgs.INotificationEventArg1<LinkedListNode<T>>> {}
	
    public void dispose() {
    	super.dispose();
    	if(NodeRemoved!=null) {
    		NodeRemoved.dispose();
    	}
    	NodeRemoved=null;
    	if(NodeAdded!= null) {
    		NodeAdded.dispose();
    	}
    	NodeAdded=null;
    }
    

    /**
     * EventHandler for notifying when a node was removed
     */
    public EventDelegate<NodeChangedEventHandler<T>> NodeRemoved = new EventDelegate<>();
    /**
     * EventHandler for notifying when nodes are cleared
     */
    public EventDelegate OnCleared = new EventDelegate();
    /**
     * EventHandler for notifying when a node was added.
     */
    public EventDelegate<NodeChangedEventHandler<T>> NodeAdded = new EventDelegate<>();

    /**
     * Notify list a node was removed.
     * @param node
     */
	void NotifyNodeRemoved(BaseLinkedListNode<T> node) {
        super.NotifyNodeRemoved(node);
        try {
            NodeRemoved.invoke(this, node);
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
        NodeAdded.invoke(this, node);
        super.NotifyNodeAdded(node, dir);
    }
    public LinkedListNode<T> AddFirst(T value) {
        return (LinkedListNode<T>)AddFirst(new LinkedListNode<>(this, value));
    }
    /**
     * Add value to the last of list.
     * @param value
     * @return new node
     */
    public LinkedListNode<T> AddLast(T value) {
        return (LinkedListNode<T>)AddLast(new LinkedListNode<>(this, value));
    }

    public LinkedListNode<T> RemoveFirst() {
        return (LinkedListNode<T>)super.RemoveFirst();
    }

    public BaseLinkedListNode<T> RemoveLast() {
        return (LinkedListNode<T>)super.RemoveLast();
    }

    public void Clear() {
        super.Clear();
        this.OnCleared.invoke(this);
    }

}