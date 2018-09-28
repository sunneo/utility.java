package com.example.delegate;


import com.example.notification.INotification;
import com.example.notification.INotificationEventArgs;

import java.util.Vector;

/**
 * Created by sunneo on 2018/7/30.
 */

public class EventDelegate<T extends INotification> {
    Vector<T> invocationList = new Vector<>();

    public synchronized  Vector<T> getInvocationList(){
        return invocationList;
    }

    public synchronized T addDelegate(T t){
        invocationList.add(t);
        return t;
    }

    public synchronized boolean removeDelegate(T t){
        return invocationList.remove(t);
    }

    public synchronized void clear(){
        invocationList.clear();
    }

    public void invoke(Object sender,INotificationEventArgs args){
        Vector<T> clone = getInvocationList();
        for(int i=0; i<clone.size(); ++i){
            T t = clone.get(i);
            t.perform(sender,args);
        }
    }
    public void invoke(Object sender,Object... args){
        INotificationEventArgs arglist = new INotificationEventArgs();
        arglist.object = args;
        this.invoke(sender,arglist);
    }
}
